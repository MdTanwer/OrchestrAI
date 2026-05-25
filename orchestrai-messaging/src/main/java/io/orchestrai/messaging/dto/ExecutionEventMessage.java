package io.orchestrai.messaging.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

import io.orchestrai.core.model.ExecutionEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Kafka payload for {@link io.orchestrai.messaging.KafkaTopics#EXECUTION_EVENTS}. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionEventMessage {

    @JsonIgnore
    private ExecutionEvent executionEvent;

    public static ExecutionEventMessage from(ExecutionEvent executionEvent) {
        return new ExecutionEventMessage(executionEvent);
    }

    public ExecutionEvent toExecutionEvent() {
        return executionEvent;
    }

    @JsonCreator
    public static ExecutionEventMessage deserialize(ExecutionEvent executionEvent) {
        return from(executionEvent);
    }

    @JsonValue
    public ExecutionEvent jsonValue() {
        return executionEvent;
    }
}
