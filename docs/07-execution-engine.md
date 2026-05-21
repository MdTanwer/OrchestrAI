# 07 — Execution Engine

## Execution Lifecycle

```
   ┌─────────┐
   │ CREATED │
   └────┬────┘
        │ start()
        ▼
   ┌─────────┐         ┌──────────┐
   │ RUNNING ├────────►│ PAUSED   │ (waiting for human)
   └────┬────┘         └────┬─────┘
        │                   │ resume()
        │                   ▼
        │              ┌─────────┐
        ├─────────────►│ RUNNING │
        │              └────┬────┘
        │                   │
        │ all tasks ok      │ task failed
        ▼                   ▼
   ┌─────────┐         ┌────────┐
   │ SUCCESS │         │ FAILED │
   └─────────┘         └────────┘
        ▲                   ▲
        │  cancel()         │
        └──┬────────────────┘
           │
      ┌────┴──────┐
      │ CANCELLED │
      └───────────┘
```

---

## Task Execution Flow

### Step-by-step

1. Engine receives execution start request
2. Load flow definition
3. Validate inputs against flow schema
4. Create Execution record (state: CREATED)
5. Initialize execution context (variables, secrets)
6. Mark state: RUNNING
7. For each task in flow:
   - a. Resolve inputs (interpolate expressions)
   - b. Check `if` condition — skip if false
   - c. Create TaskRun (state: CREATED)
   - d. Publish to Kafka `task-queue`
   - e. Wait for result on `task-results` topic
   - f. Apply retry policy if failed
   - g. Store outputs in context
   - h. Mark TaskRun (SUCCESS / FAILED)
   - i. If failed and no retry left → execute `onFailure`
8. Mark Execution (SUCCESS / FAILED)
9. Emit final execution event

---

## Expression Resolution

When a task contains `{{ outputs.task1.response }}`, the engine:

1. Tokenizes the expression
2. Looks up `outputs.task1` in execution context
3. Accesses `.response` field
4. Replaces `{{ }}` with the actual value

**Implementation:** Use **JEXL** (Apache Commons) or a custom parser.

---

## Retry Logic

```yaml
retry:
  maxAttempts: 3
  backoff: exponential   # or: fixed, linear
  initialDelay: "1s"
  maxDelay: "30s"
  retryOn:
    - TIMEOUT
    - HTTP_5XX
```

**Pseudocode:**

```java
int attempt = 1;
while (attempt <= retry.maxAttempts) {
    try {
        result = executeTask(task);
        return result;
    } catch (RetryableException e) {
        if (attempt == retry.maxAttempts) throw e;
        Duration delay = calculateBackoff(attempt, retry);
        sleep(delay);
        attempt++;
    }
}
```

---

## Parallel Execution

```yaml
- id: parallel-block
  type: core.parallel
  tasks:
    - id: a
      type: openai.chat
    - id: b
      type: anthropic.chat
```

**Engine behavior:**

1. Identify all sub-tasks
2. Publish all to Kafka simultaneously
3. Wait for ALL to complete (using `CompletableFuture.allOf`)
4. Collect outputs

---

## Concurrency Model

- Each worker has a thread pool (default: 10 threads)
- Tasks are async (non-blocking I/O)
- Kafka consumer groups handle distribution
- Idempotency keys prevent duplicate execution
