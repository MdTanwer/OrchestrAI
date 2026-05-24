# 05 — System Architecture

OrchestrAI utilizes a **fully event-driven, reactive, and stateless orchestration architecture** heavily inspired by Kestra. It is built entirely on top of **Quarkus** and **Apache Kafka**, ensuring extreme throughput, sub-millisecond state transitions, and 100% resilience against component failures.

---

## High-Level Architecture

The system is split into small, stateless, and horizontally scalable microservices that communicate asynchronously using a transactional event queue (Kafka).

```
┌─────────────────────────────────────────────────────────┐
│                      USERS                              │
│  (Web UI, CLI, API Clients, Webhooks)                   │
└──────────────────┬──────────────────────────────────────┘
                   │ HTTPS / SSE
                   ▼
┌─────────────────────────────────────────────────────────┐
│                  API SERVER (Quarkus)                   │
│  - Flow CRUD & Validation                               │
│  - Execution Triggering & SSE Log Streamer              │
└──────┬────────────────────────────────┬─────────────────┘
       │                                │
       │ writes                         │ publishes
       ▼                                ▼
┌──────────────────┐         ┌──────────────────────────────┐
│    DATABASE      │         │     KAFKA EVENT QUEUE        │
│  (PostgreSQL)    │         │  - executions                │
│  - Flows         │         │  - task-runs (WorkerTask)    │
│  - Executions    │         │  - task-results (TaskResult) │
│  - Secrets       │         │  - task-logs (Structured)    │
│  - Logs          │         └──────┬────────────────┬──────┘
└──────────────────┘                │                │
                                    │ consumes       │ consumes
                                    ▼                ▼
                         ┌────────────────────┐   ┌────────────────────┐
                         │ EXECUTOR (Quarkus) │   │ WORKER (Quarkus)   │
                         │ - Reactive Engine  │   │ - Stateless Worker │
                         │ - State Machine    │   │ - Plugin Runner    │
                         └────────────────────┘   └────────────────────┘
```

---

## Core Components

All OrchestrAI backend services are built using the **Quarkus** framework, utilizing Mutiny for reactive programming and optimized GraalVM Native Image compilation.

### 1. API Server (Quarkus)
*   **Role:** The entry point for all user interactions.
*   **Responsibilities:**
    *   Exposes a high-performance REST API for managing flows, triggering executions, and registering custom plugins.
    *   Validates flow YAML structures against registered plugin schemas.
    *   Publishes execution requests to the `executions` Kafka queue.
    *   Subscribes directly to the `task-logs` Kafka topic to stream real-time execution logs directly to users via **Server-Sent Events (SSE)**, completely bypassing PostgreSQL for streaming reads.
    *   Consumes the `task-logs` Kafka topic and performs buffered, asynchronous bulk-inserts to the PostgreSQL `logs` table, ensuring high-performance log persistence without blocking worker execution.

### 2. The Executor (Quarkus Core Engine)
*   **Role:** The centralized, reactive "brain" of the platform.
*   **Design:** Inspired by Kestra's core runner, the Executor is **entirely stateless** and reactive. It holds **no state in JVM memory** and blocks **zero threads** while waiting for tasks.
*   **Responsibilities:**
    *   Listens to the `executions` and `task-results` Kafka queues.
    *   Upon consuming a completion event, it locks the execution record in the database, runs pure state transitions, and resolves expressions for subsequent steps.
    *   Determines which tasks are ready to run next and dispatches them as events to the `task-runs` queue.
    *   If a task or flow fails, the Executor processes retries, timeouts, or `onFailure` fallback logic in a stateless manner.
    *   If all tasks succeed, it updates the execution state to `SUCCESS` in the database and terminates the event loop.

### 3. Worker Nodes (Quarkus Worker)
*   **Role:** The horizontal task executors.
*   **Design:** Completely stateless runner. Workers do **not** have direct database connections (PostgreSQL) and have **no knowledge of the overall flow structure**.
*   **Responsibilities:**
    *   Consume single task execution payloads (`TaskRun`) from the `task-runs` Kafka queue.
    *   Instantiate the appropriate plugin (e.g., `openai.chat`, `http.request`).
    *   Execute the task inside a sandbox environment using the `Plugin.execute()` SDK.
    *   Publish task outputs, token metrics, and cost metrics to the `task-results` queue.
    *   Publish real-time task logs as structured JSON events to the `task-logs` queue.

### 4. The Scheduler (Quarkus Scheduler)
*   **Role:** Time-based trigger monitor.
*   **Responsibilities:**
    *   Watches registered Flow YAML definitions for `schedule.cron` triggers.
    *   Evaluates scheduling constraints.
    *   Publishes newly triggered execution templates directly into the `executions` Kafka queue.

---

## Data Flow Sequence (Event-Driven Execution)

To execute a workflow, OrchestrAI orchestrates state entirely through asynchronous event loops:

```
 User      API Server       Kafka Topics       Stateless Executor     Worker Node
  │            │                 │                     │                   │
  │─── POST ──►│                 │                     │                   │
  │  (Execute) │─── Publish ────►│                     │                   │
  │            │  (executions)   │                     │                   │
  │◄── 202 ────│                 │                     │                   │
  │ (Accepted) │                 │◄─── Consume Event ──│                   │
  │            │                 │     (executions)    │                   │
  │            │                 │                     │                   │
  │            │                 │─── Publish Task ───►│                   │
  │            │                 │    (task-runs)      │                   │
  │            │                 │                     │◄── Consume Task ──│
  │            │                 │                     │    (task-runs)    │
  │            │                 │                     │                   │
  │            │                 │                     │                   │── Execute task
  │            │                 │                     │                   │── Emit logs
  │            │                 │◄─── Publish Logs ───┼───────────────────│
  │            │                 │     (task-logs)     │                   │
  │            │◄── Consume ─────│                     │                   │
  │            │    (task-logs)  │                     │                   │
  │◄─── SSE ───│                 │                     │                   │
  │  (Log)     │                 │                     │                   │
  │            │                 │◄─── Publish Result ─┼───────────────────│
  │            │                 │     (task-results)  │                   │
  │            │                 │                     │                   │
  │            │                 │◄─── Consume Result ─│                   │
  │            │                 │     (task-results)  │                   │
  │            │                 │                     │                   │
  │            │                 │─── Publish Next ───►│                   │
  │            │                 │    (task-runs)      │                   │
```

---

## Kafka Queue Topics & Schema

All queues are implemented as highly partitionable Kafka topics:

| Topic | Event Payload | Producer | Consumer | Description |
|-------|---------------|----------|----------|-------------|
| `executions` | `Execution` | API / Scheduler | Executor | Starts a new execution or triggers a manual resume. |
| `task-runs` | `TaskRun` | Executor | Workers | Standard worker tasks distributed across workers. |
| `task-results` | `TaskResult` | Workers | Executor | Completion status, outputs, token metrics, and costs. |
| `task-logs` | `LogEntry` | Workers | API Server | Structured real-time execution logs for SSE streaming. |
| `execution-events` | `ExecutionEvent` | Executor | API Server | Live execution state transitions for dashboard metrics. |
| `dead-letter` | `TaskRun` | Workers | Operations | Failed tasks after exhaustion of all retry attempts. |

---

## Design Principles (Inspired by Kestra)

1.  **Strict Statelessness:** Workers and Executors maintain zero in-memory execution state. If any node dies, another node picks up the partition and continues seamlessly.
2.  **Zero-Thread-Blocking:** The Executor never blocks a thread waiting for Kafka responses. Every transition is triggered by a fresh message consumed from a topic.
3.  **Late Secrets Injection:** Workers resolve credentials from dynamic context managers at execution time, preventing secrets from passing through database logs or Kafka queues in plaintext.
4.  **Bulk Log Buffering:** Workers pipe logs as structured events over Kafka. The DB is decoupled from high-throughput worker logs, saving transactional performance.
5.  **GraalVM Friendly:** Eliminating dynamic runtime reflection and standardizing on build-time metadata registry ensures 100% compatibility with Native compilation.

---

## Flow demo: distributed execution

Architecture above is the reference model. For a **concrete flow** that benefits from multiple workers (parallel LLM tasks → multiple `task-runs` messages), see:

- [`examples/16-distributed-document-review.yaml`](../examples/16-distributed-document-review.yaml)
- [`examples/DISTRIBUTED.md`](../examples/DISTRIBUTED.md) — Kafka topics, scaling replicas, which YAML patterns fan out to workers
