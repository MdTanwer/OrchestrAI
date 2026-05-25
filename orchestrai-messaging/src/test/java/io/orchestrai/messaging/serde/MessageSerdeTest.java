package io.orchestrai.messaging.serde;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.orchestrai.core.enums.ExecutionState;
import io.orchestrai.core.enums.LogLevel;
import io.orchestrai.core.enums.TaskRunState;
import io.orchestrai.core.enums.TriggerType;
import io.orchestrai.core.model.Execution;
import io.orchestrai.core.model.ExecutionEvent;
import io.orchestrai.core.model.LogEntry;
import io.orchestrai.core.model.TaskResult;
import io.orchestrai.core.model.TaskRun;
import io.orchestrai.messaging.KafkaTopics;
import io.orchestrai.messaging.dto.ExecutionEventMessage;
import io.orchestrai.messaging.dto.ExecutionMessage;
import io.orchestrai.messaging.dto.LogEntryMessage;
import io.orchestrai.messaging.dto.TaskResultMessage;
import io.orchestrai.messaging.dto.TaskRunMessage;

class MessageSerdeTest {

    @Test
    void kafkaTopics_containsAllContractTopics() {
        assertEquals(6, KafkaTopics.all().size());
        assertTrue(KafkaTopics.isKnown(KafkaTopics.EXECUTIONS));
        assertTrue(KafkaTopics.isKnown(KafkaTopics.TASK_RUNS));
        assertTrue(KafkaTopics.isKnown(KafkaTopics.TASK_RESULTS));
        assertTrue(KafkaTopics.isKnown(KafkaTopics.TASK_LOGS));
        assertTrue(KafkaTopics.isKnown(KafkaTopics.EXECUTION_EVENTS));
        assertTrue(KafkaTopics.isKnown(KafkaTopics.DEAD_LETTER));
    }

    @Test
    void taskRunMessage_roundTrip_json() throws Exception {
        UUID taskRunId = UUID.fromString("770e8400-e29b-41d4-a716-446655440002");
        UUID executionId = UUID.fromString("660e8400-e29b-41d4-a716-446655440001");
        Instant started = Instant.parse("2026-05-24T10:00:00Z");

        TaskRun original = TaskRun.builder()
                .id(taskRunId)
                .executionId(executionId)
                .taskId("answer")
                .taskType("openai.chat")
                .state(TaskRunState.RUNNING)
                .attempt(1)
                .inputs(Map.of("prompt", "Summarize release notes"))
                .startedAt(started)
                .workerId("worker-1")
                .parentTaskId("fetch-docs")
                .createdAt(started)
                .build();

        TaskRunMessage message = TaskRunMessage.from(original);
        String json = MessageSerde.serialize(message);
        TaskRunMessage restored = MessageSerde.deserializeTaskRun(json);

        assertEquals(original, restored.toTaskRun());
        assertEquals(original, restored.getTaskRun());

        TaskRun fromBytes = MessageSerde.deserializeTaskRun(MessageSerde.toBytes(message)).toTaskRun();
        assertEquals(original, fromBytes);
    }

    @Test
    void executionMessage_roundTrip_json() throws Exception {
        Execution original = Execution.builder()
                .id(UUID.randomUUID())
                .flowId(UUID.randomUUID())
                .namespace("examples.demo")
                .state(ExecutionState.CREATED)
                .triggerType(TriggerType.MANUAL)
                .inputs(Map.of("question", "hello"))
                .createdAt(Instant.parse("2026-05-24T09:00:00Z"))
                .build();

        String json = MessageSerde.serialize(ExecutionMessage.from(original));
        Execution restored = MessageSerde.deserializeExecution(json).toExecution();

        assertEquals(original.getNamespace(), restored.getNamespace());
        assertEquals(original.getState(), restored.getState());
        assertEquals(original.getTriggerType(), restored.getTriggerType());
        assertEquals(original.getInputs(), restored.getInputs());
    }

    @Test
    void taskResultMessage_roundTrip_json() throws Exception {
        TaskResult original = TaskResult.builder()
                .taskRunId(UUID.randomUUID())
                .executionId(UUID.randomUUID())
                .taskId("answer")
                .state(TaskRunState.SUCCESS)
                .outputs(Map.of("response", "done"))
                .tokensUsed(42)
                .costUsd(new BigDecimal("0.0042"))
                .durationMs(1500L)
                .workerId("worker-2")
                .build();

        String json = MessageSerde.serialize(TaskResultMessage.from(original));
        TaskResult restored = MessageSerde.deserializeTaskResult(json).toTaskResult();

        assertEquals(original, restored);
    }

    @Test
    void logEntryMessage_roundTrip_json() throws Exception {
        LogEntry original = LogEntry.builder()
                .executionId(UUID.randomUUID())
                .taskRunId(UUID.randomUUID())
                .level(LogLevel.INFO)
                .message("Calling OpenAI")
                .metadata(Map.of("model", "gpt-4o-mini"))
                .createdAt(Instant.parse("2026-05-24T10:00:01Z"))
                .build();

        String json = MessageSerde.serialize(LogEntryMessage.from(original));
        LogEntry restored = MessageSerde.deserializeLogEntry(json).toLogEntry();

        assertEquals(original, restored);
    }

    @Test
    void executionEventMessage_roundTrip_json() throws Exception {
        ExecutionEvent original = ExecutionEvent.builder()
                .executionId(UUID.randomUUID())
                .state(ExecutionState.RUNNING)
                .timestamp(Instant.parse("2026-05-24T10:00:05Z"))
                .totalTokens(100L)
                .totalCostUsd(new BigDecimal("0.01"))
                .build();

        String json = MessageSerde.serialize(ExecutionEventMessage.from(original));
        ExecutionEvent restored = MessageSerde.deserializeExecutionEvent(json).toExecutionEvent();

        assertEquals(original, restored);
    }
}
