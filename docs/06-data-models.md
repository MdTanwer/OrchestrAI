# 06 — Data Models

OrchestrAI utilizes **PostgreSQL** for transactional persistence of flow metadata, execution metrics, and audit history. High-throughput logs and real-time streams are decoupled via Kafka, but final state summaries are written to DB records.

---

## PostgreSQL Database Schema

To prevent transactional lockouts and orphaned entries, all tables utilize cascading referential integrity constraints.

### `flows` (Flow Metadata - Immutable per version)
```sql
CREATE TABLE flows (
  id              UUID PRIMARY KEY,
  flow_id         VARCHAR(100) NOT NULL,    -- User-defined ID (e.g. "moderator")
  namespace       VARCHAR(100) NOT NULL,    -- Logical grouping (e.g. "ai.prod")
  version         INT NOT NULL DEFAULT 1,   -- Immutable auto-increment version
  yaml_source     TEXT NOT NULL,            -- Original YAML source code
  parsed_json     JSONB NOT NULL,           -- Parsed schema tree for fast reading
  description     TEXT,
  labels          JSONB,
  created_at      TIMESTAMP NOT NULL,
  updated_at      TIMESTAMP NOT NULL,
  created_by      UUID,
  UNIQUE(namespace, flow_id, version)
);

CREATE INDEX idx_flows_lookup ON flows(namespace, flow_id);
```

### `executions` (Flow Executions)
```sql
CREATE TABLE executions (
  id              UUID PRIMARY KEY,
  flow_id         UUID REFERENCES flows(id) ON DELETE CASCADE,
  namespace       VARCHAR(100) NOT NULL,    -- Denormalized for fast filtering
  state           VARCHAR(20) NOT NULL,     -- CREATED, RUNNING, SUCCESS, FAILED, CANCELLED, PAUSED
  inputs          JSONB,                    -- Trigger input parameters
  outputs         JSONB,                    -- Accumulated outputs of all tasks
  trigger_type    VARCHAR(20),              -- MANUAL, CRON, WEBHOOK, EVENT
  trigger_data    JSONB,
  started_at      TIMESTAMP,
  ended_at        TIMESTAMP,
  duration_ms     BIGINT,
  total_cost_usd  DECIMAL(10, 6),
  total_tokens    BIGINT,
  error_message   TEXT,
  created_at      TIMESTAMP NOT NULL
);

CREATE INDEX idx_executions_state ON executions(state);
CREATE INDEX idx_executions_flow ON executions(flow_id);
```

### `task_runs` (Individual Task State)
```sql
CREATE TABLE task_runs (
  id              UUID PRIMARY KEY,
  execution_id    UUID REFERENCES executions(id) ON DELETE CASCADE,
  task_id         VARCHAR(100) NOT NULL,    -- Task ID from YAML
  task_type       VARCHAR(100) NOT NULL,    -- Plugin identifier (e.g. "openai.chat")
  state           VARCHAR(20) NOT NULL,     -- CREATED, RUNNING, SUCCESS, FAILED, CANCELLED
  attempt         INT NOT NULL DEFAULT 1,   -- Current retry attempt
  inputs          JSONB,                    -- Resolved inputs (without plaintext secrets!)
  outputs         JSONB,                    -- Outputs returned by the worker
  started_at      TIMESTAMP,
  ended_at        TIMESTAMP,
  duration_ms     BIGINT,
  tokens_used     INT,
  cost_usd        DECIMAL(10, 6),
  error_message   TEXT,
  worker_id       VARCHAR(100),             -- Identifies which worker executed this task
  created_at      TIMESTAMP NOT NULL
);

CREATE INDEX idx_taskruns_exec ON task_runs(execution_id);
```

### `logs` (Execution Logs - Decoupled bulk-inserts)
```sql
CREATE TABLE logs (
  id              BIGSERIAL PRIMARY KEY,
  execution_id    UUID REFERENCES executions(id) ON DELETE CASCADE,
  task_run_id     UUID REFERENCES task_runs(id) ON DELETE CASCADE,
  level           VARCHAR(10) NOT NULL,     -- DEBUG, INFO, WARN, ERROR
  message         TEXT NOT NULL,
  metadata        JSONB,                    -- Contextual fields (e.g. model name)
  created_at      TIMESTAMP NOT NULL
);

CREATE INDEX idx_logs_exec ON logs(execution_id);
CREATE INDEX idx_logs_taskrun ON logs(task_run_id);
```

### `triggers` (Flow Scheduling States)
```sql
CREATE TABLE triggers (
  id              UUID PRIMARY KEY,
  flow_id         UUID REFERENCES flows(id) ON DELETE CASCADE,
  trigger_id      VARCHAR(100) NOT NULL,
  type            VARCHAR(50) NOT NULL,     -- e.g. "schedule.cron"
  config          JSONB NOT NULL,
  enabled         BOOLEAN NOT NULL DEFAULT TRUE,
  last_triggered  TIMESTAMP,
  next_trigger    TIMESTAMP,
  created_at      TIMESTAMP NOT NULL
);
```

### `secrets` (Encrypted Secrets Store)
```sql
CREATE TABLE secrets (
  id              UUID PRIMARY KEY,
  namespace       VARCHAR(100) NOT NULL,
  key             VARCHAR(100) NOT NULL,
  encrypted_value TEXT NOT NULL,            -- AES-256-GCM encrypted base64 payload
  created_at      TIMESTAMP NOT NULL,
  UNIQUE(namespace, key)
);
```

---

## Java Domain Models

The domain models are compiled as immutable Quarkus schemas, optimized for Jackson parsing and build-time reflection registration.

### `Flow` Definition Model
```java
package io.orchestrai.core.model;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Flow {
    private UUID id;
    private String flowId;
    private String namespace;
    private int version;
    private String description;
    private Map<String, String> labels;
    private List<Input> inputs;
    private Map<String, Object> variables;
    private List<Trigger> triggers;
    private List<Task> tasks;
    private List<Task> onFailure;
    
    // Getters, Setters, Builders
}
```

### `Task` Definition Model
```java
package io.orchestrai.core.model;

import java.time.Duration;
import java.util.Map;

public class Task {
    private String id;
    private String type;
    private String description;
    private Duration timeout;
    private RetryPolicy retry;
    private String ifCondition;
    private Map<String, Object> config; // Raw configuration parameters to be parsed by workers
    
    // Getters, Setters, Builders
}
```

### `Execution` State Model
```java
package io.orchestrai.core.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Execution {
    private UUID id;
    private UUID flowId;
    private String namespace; // Denormalized for state resolution
    private ExecutionState state;
    private Map<String, Object> inputs;
    private Map<String, Object> outputs;
    private TriggerType triggerType;
    private Instant startedAt;
    private Instant endedAt;
    private List<TaskRun> taskRuns;
    private BigDecimal totalCostUsd;
    private long totalTokens;
    
    // Helper to evaluate if a subtask is complete reactively
    public boolean hasTaskCompleted(String taskId) {
        return taskRuns.stream()
            .anyMatch(tr -> tr.getTaskId().equals(taskId) && tr.getState().isTerminal());
    }
}

public enum ExecutionState {
    CREATED, RUNNING, SUCCESS, FAILED, CANCELLED, PAUSED;
    
    public boolean isTerminal() {
        return this == SUCCESS || this == FAILED || this == CANCELLED;
    }
}
```

### `TaskRun` Payload Model (Dispatched over Kafka as `WorkerTask`)
```java
package io.orchestrai.core.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class TaskRun {
    private UUID id;
    private UUID executionId;
    private String taskId;
    private String taskType;
    private TaskRunState state;
    private int attempt;
    private Map<String, Object> inputs;   // Resolved values ready for execution
    private Map<String, Object> outputs;  // Set by worker
    private Instant startedAt;
    private Instant endedAt;
    private long durationMs;
    private int tokensUsed;
    private BigDecimal costUsd;
    private String errorMessage;
    private String workerId;
}

public enum TaskRunState {
    CREATED, RUNNING, SUCCESS, FAILED;
    
    public boolean isTerminal() {
        return this == SUCCESS || this == FAILED;
    }
}
```
