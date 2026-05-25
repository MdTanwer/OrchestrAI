package io.orchestrai.messaging.serde;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.orchestrai.core.jackson.ObjectMappers;
import io.orchestrai.messaging.dto.ExecutionEventMessage;
import io.orchestrai.messaging.dto.ExecutionMessage;
import io.orchestrai.messaging.dto.LogEntryMessage;
import io.orchestrai.messaging.dto.TaskResultMessage;
import io.orchestrai.messaging.dto.TaskRunMessage;

/**
 * Jackson JSON serialization for Kafka message DTOs.
 * Wire format matches {@link io.orchestrai.core.model} types (no extra envelope).
 */
public final class MessageSerde {

    private static final ObjectMapper MAPPER = ObjectMappers.json();

    private MessageSerde() {
    }

    public static ObjectMapper mapper() {
        return MAPPER;
    }

    public static String toJson(Object value) throws JsonProcessingException {
        return MAPPER.writeValueAsString(value);
    }

    public static byte[] toBytes(Object value) throws JsonProcessingException {
        return MAPPER.writeValueAsBytes(value);
    }

    public static <T> T fromJson(String json, Class<T> type) throws JsonProcessingException {
        return MAPPER.readValue(json, type);
    }

    public static <T> T fromBytes(byte[] bytes, Class<T> type) throws IOException {
        return MAPPER.readValue(bytes, type);
    }

    public static String serialize(TaskRunMessage message) throws JsonProcessingException {
        return toJson(message);
    }

    public static TaskRunMessage deserializeTaskRun(String json) throws JsonProcessingException {
        return fromJson(json, TaskRunMessage.class);
    }

    public static TaskRunMessage deserializeTaskRun(byte[] bytes) throws IOException {
        return fromBytes(bytes, TaskRunMessage.class);
    }

    public static String serialize(ExecutionMessage message) throws JsonProcessingException {
        return toJson(message);
    }

    public static ExecutionMessage deserializeExecution(String json) throws JsonProcessingException {
        return fromJson(json, ExecutionMessage.class);
    }

    public static String serialize(TaskResultMessage message) throws JsonProcessingException {
        return toJson(message);
    }

    public static TaskResultMessage deserializeTaskResult(String json) throws JsonProcessingException {
        return fromJson(json, TaskResultMessage.class);
    }

    public static String serialize(LogEntryMessage message) throws JsonProcessingException {
        return toJson(message);
    }

    public static LogEntryMessage deserializeLogEntry(String json) throws JsonProcessingException {
        return fromJson(json, LogEntryMessage.class);
    }

    public static String serialize(ExecutionEventMessage message) throws JsonProcessingException {
        return toJson(message);
    }

    public static ExecutionEventMessage deserializeExecutionEvent(String json) throws JsonProcessingException {
        return fromJson(json, ExecutionEventMessage.class);
    }
}
