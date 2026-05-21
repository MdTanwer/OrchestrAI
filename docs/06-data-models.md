# 06 — Data Models

## Database: PostgreSQL

---

## Tables

### `flows`

```sql
CREATE TABLE flows (
  id              UUID PRIMARY KEY,
  flow_id         VARCHAR(100) NOT NULL,    -- user-defined id
  namespace       VARCHAR(100) NOT NULL,
  version         INT NOT NULL DEFAULT 1,
  yaml_source     TEXT NOT NULL,
  parsed_json     JSONB NOT NULL,
  description     TEXT,
  labels          JSONB,
  created_at      TIMESTAMP NOT NULL,
  updated_at      TIMESTAMP NOT NULL,
  created_by      UUID,
  UNIQUE(namespace, flow_id, version)
);
```

### `executions`

```sql
CREATE TABLE executions (
  id              UUID PRIMARY KEY,
  flow_id         UUID REFERENCES flows(id),
  namespace       VARCHAR(100) NOT NULL,
  state           VARCHAR(20) NOT NULL,  -- CREATED, RUNNING, SUCCESS, FAILED, CANCELLED, PAUSED
  inputs          JSONB,
  outputs         JSONB,
  trigger_type    VARCHAR(20),           -- MANUAL, CRON, WEBHOOK, EVENT
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

### `task_runs`

```sql
CREATE TABLE task_runs (
  id              UUID PRIMARY KEY,
  execution_id    UUID REFERENCES executions(id),
  task_id         VARCHAR(100) NOT NULL,  -- from YAML
  task_type       VARCHAR(100) NOT NULL,  -- plugin type
  state           VARCHAR(20) NOT NULL,
  attempt         INT NOT NULL DEFAULT 1,
  inputs          JSONB,
  outputs         JSONB,
  started_at      TIMESTAMP,
  ended_at        TIMESTAMP,
  duration_ms     BIGINT,
  tokens_used     INT,
  cost_usd        DECIMAL(10, 6),
  error_message   TEXT,
  worker_id       VARCHAR(100),
  created_at      TIMESTAMP NOT NULL
);

CREATE INDEX idx_taskruns_exec ON task_runs(execution_id);
```

### `logs`

```sql
CREATE TABLE logs (
  id              BIGSERIAL PRIMARY KEY,
  execution_id    UUID NOT NULL,
  task_run_id     UUID,
  level           VARCHAR(10) NOT NULL,  -- DEBUG, INFO, WARN, ERROR
  message         TEXT NOT NULL,
  metadata        JSONB,
  created_at      TIMESTAMP NOT NULL
);

CREATE INDEX idx_logs_exec ON logs(execution_id);
CREATE INDEX idx_logs_taskrun ON logs(task_run_id);
```

### `triggers`

```sql
CREATE TABLE triggers (
  id              UUID PRIMARY KEY,
  flow_id         UUID REFERENCES flows(id),
  trigger_id      VARCHAR(100) NOT NULL,
  type            VARCHAR(50) NOT NULL,
  config          JSONB NOT NULL,
  enabled         BOOLEAN NOT NULL DEFAULT TRUE,
  last_triggered  TIMESTAMP,
  next_trigger    TIMESTAMP,
  created_at      TIMESTAMP NOT NULL
);
```

### `plugins`

```sql
CREATE TABLE plugins (
  id              UUID PRIMARY KEY,
  type            VARCHAR(100) UNIQUE NOT NULL,
  name            VARCHAR(100) NOT NULL,
  version         VARCHAR(20) NOT NULL,
  description     TEXT,
  schema          JSONB NOT NULL,
  is_builtin      BOOLEAN NOT NULL DEFAULT FALSE,
  created_at      TIMESTAMP NOT NULL
);
```

### `secrets`

```sql
CREATE TABLE secrets (
  id              UUID PRIMARY KEY,
  namespace       VARCHAR(100) NOT NULL,
  key             VARCHAR(100) NOT NULL,
  encrypted_value TEXT NOT NULL,
  created_at      TIMESTAMP NOT NULL,
  UNIQUE(namespace, key)
);
```

---

## Java Domain Models

### Flow

```java
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
}
```

### Task

```java
public class Task {
    private String id;
    private String type;
    private String description;
    private Duration timeout;
    private RetryPolicy retry;
    private String ifCondition;
    private Map<String, Object> config; // plugin-specific
}
```

### Execution

```java
public class Execution {
    private UUID id;
    private UUID flowId;
    private ExecutionState state;
    private Map<String, Object> inputs;
    private Map<String, Object> outputs;
    private TriggerType triggerType;
    private Instant startedAt;
    private Instant endedAt;
    private List<TaskRun> taskRuns;
    private BigDecimal totalCostUsd;
    private long totalTokens;
}

public enum ExecutionState {
    CREATED, RUNNING, SUCCESS, FAILED, CANCELLED, PAUSED
}
```
