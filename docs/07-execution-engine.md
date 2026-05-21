# 07 — Execution Engine

The OrchestrAI Execution Engine is a reactive, event-driven state machine built on Quarkus and Apache Kafka. It coordinates the lifecycle of complex multi-agent workflows without holding active threads, blocking resources, or keeping execution state in memory.

---

## Execution Lifecycle

The execution lifecycle consists of deterministic state transitions. Transitions are triggered exclusively by Kafka events and are persisted to the database inside transactional blocks.

```
       ┌───────────┐
       │  CREATED  │
       └─────┬─────┘
             │ start()
             ▼
       ┌───────────┐   pause()   ┌───────────┐
       │  RUNNING  ├────────────►│  PAUSED   │ (Waiting for approval)
       └─────┬─────┘◄────────────┤─────┬─────┘
             │         resume()  │     │
             │                   │     │ cancel()
             ├───────────────────┼─────┘
             │                   │
             │ all tasks ok      │ task failed (no retry)
             ▼                   ▼
       ┌───────────┐       ┌───────────┐
       │  SUCCESS  │       │  FAILED   │
       └───────────┘       └───────────┘
             ▲                   ▲
             │ cancel()          │ cancel()
             └─────────┬─────────┘
                       │
                 ┌─────┴─────┐
                 │ CANCELLED │ (Terminal)
                 └───────────┘
```

> [!NOTE]
> `SUCCESS`, `FAILED`, and `CANCELLED` are terminal states. A running or paused execution can transition to `CANCELLED` at any time, but once cancelled, no further transitions are allowed.

---

## The Reactive Execution Loop

Instead of running an active thread that blocks waiting for child tasks to complete, the Executor runs as a **stateless message processor**. It subscribes to Kafka topics (`executions` and `task-results`) and executes a quick, non-blocking state computation cycle every time an event arrives.

### The Lifecycle Event Handlers

The Executor implements three primary reactive handlers:

1.  **Execution Trigger Handler (consumes `executions`):**
    *   Fires when a flow is executed manually, by a trigger, or resumed.
    *   Creates/locks the `Execution` record in the database.
    *   Evaluates the flow's initial tasks, resolves expressions, and dispatches the ready tasks to the `task-runs` queue.
2.  **Worker Result Handler (consumes `task-results`):**
    *   Fires when a worker completes a task and publishes its results.
    *   Locks the parent `Execution` in the database.
    *   Applies the task's output to the execution context.
    *   Evaluates conditional blocks (`core.if`, `core.parallel`) to check if subsequent tasks are ready.
    *   If next tasks are found, dispatches them to `task-runs`. If the flow is complete, marks the execution `SUCCESS`.
3.  **Command Handler (consumes `executions` with admin commands):**
    *   Fires when a user issues an execution cancellation or pause command.
    *   Instantly shifts the database state and emits appropriate cancellation signals.

---

## Task Execution Flow (Step-by-Step)

Rather than keeping state in a blocking JVM thread, the stateless execution flow operates as follows:

```
┌──────────────┐          ┌──────────────┐          ┌──────────────┐          ┌──────────────┐
│  API/Trigger │          │   Executor   │          │ Kafka Queue  │          │ Worker Node  │
└──────┬───────┘          └──────┬───────┘          └──────┬───────┘          └──────┬───────┘
       │  Trigger execution      │                         │                         │
       │────────────────────────►│                         │                         │
       │                         │─── Lock Execution ────┐ │                         │
       │                         │    & Create in DB     │ │                         │
       │                         │◄──────────────────────┘ │                         │
       │                         │                         │                         │
       │                         │─── Resolve task 1 ─────►│                         │
       │                         │    and dispatch         │                         │
       │                         │                         │◄── Consume task 1 ──────│
       │                         │                         │    & Execute            │
       │                         │                         │    (No active engine    │
       │                         │                         │     thread blocks!)     │
       │                         │                         │                         │
       │                         │◄── Emit result ─────────┼─────────────────────────│
       │                         │    (task-results)       │                         │
       │                         │                         │                         │
       │                         │─── Lock execution ────┐ │                         │
       │                         │    & Compute next step│ │                         │
       │                         │◄──────────────────────┘ │                         │
       │                         │                         │                         │
       │                         │─── Dispatch task 2 ────►│                         │
       │                         │    (task-runs)          │                         │
```

---

## Parallel Execution (Stateless Wait)

Traditional workflow engines block the parent thread using tools like `CompletableFuture.allOf` to wait for parallel branches. In OrchestrAI, parallel execution is entirely **asynchronous and stateless**:

1.  When a task of type `core.parallel` is encountered, the Executor identifies all child tasks.
2.  The Executor creates database `TaskRun` records for **all child tasks** in the `CREATED` state.
3.  The Executor publishes all child tasks **simultaneously** to the `task-runs` Kafka queue and exits.
4.  As workers finish individual child tasks, they emit `TaskResult` events to `task-results`.
5.  On *each* incoming `TaskResult` event:
    *   The Executor loads the parent `Execution` state.
    *   It updates the state of the completed child task.
    *   It queries the DB to check if **all sibling tasks** under the `core.parallel` parent have reached a terminal state (`SUCCESS` or `FAILED`).
    *   If siblings are still running, the Executor **does nothing and terminates the cycle**.
    *   If the last sibling completes, the Executor aggregates their outputs, updates the `core.parallel` task run, and resolves the next sequential step in the flow.

---

## Expression & Secret Resolution

OrchestrAI resolves dynamic inputs at the **last possible millisecond** to protect system performance and security.

### JEXL Expression Resolution
Dynamic inputs (e.g. `{{ outputs.task1.response }}`) are evaluated using **Apache Commons JEXL** during task dispatching. The Executor pulls execution inputs, variables, and previous task outputs from the database context map, interpolates the string, and formats the output.

### Secure Secret Reference Resolution (Critical Security Pattern)
To prevent secret keys from leaking, the Executor **never decrypts secrets during task resolution**.
*   **Vulnerable Pattern (Avoid):** Resolving `{{ secret('KEY') }}` in the Executor translates the secret into plaintext *before* publishing it to Kafka, leaking credentials to Kafka logs and the database.
*   **Secure Kestra-Like Pattern (Implemented):**
    1.  If the YAML uses a secret reference, the Executor resolves it to an encrypted token block (e.g. `secret:OPENAI_API_KEY`) and publishes it to `task-runs`.
    2.  The Worker Node consumes the token reference.
    3.  Inside the worker's sandboxed environment, `Plugin.execute()` calls `ctx.getSecret("OPENAI_API_KEY")`.
    4.  The Worker's context manager decrypts the key on-the-fly from the local secrets resolver. Plaintext keys are never written to Kafka or the PostgreSQL `task_runs` database columns.
