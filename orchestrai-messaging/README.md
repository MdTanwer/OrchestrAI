# orchestrai-messaging

Shared Kafka contract: topic constants, message DTOs, and Jackson serde. Framework-agnostic (no Quarkus).

## Topics

| Constant | Topic | Payload |
|----------|-------|---------|
| `EXECUTIONS` | `executions` | `Execution` |
| `TASK_RUNS` | `task-runs` | `TaskRun` |
| `TASK_RESULTS` | `task-results` | `TaskResult` |
| `TASK_LOGS` | `task-logs` | `LogEntry` |
| `EXECUTION_EVENTS` | `execution-events` | `ExecutionEvent` |
| `DEAD_LETTER` | `dead-letter` | `TaskRun` |

## Usage

```java
TaskRunMessage msg = TaskRunMessage.from(taskRun);
String json = MessageSerde.serialize(msg);
TaskRun restored = MessageSerde.deserializeTaskRun(json).toTaskRun();
```

Wire JSON is identical to core model JSON (no wrapper object).

## Build

```bash
mvn test -pl orchestrai-messaging
```

See [docs/16-developer-setup.md](../docs/16-developer-setup.md) for JDK 17.
