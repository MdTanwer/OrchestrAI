package io.orchestrai.messaging.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

import io.orchestrai.core.model.Execution;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Kafka payload for {@link io.orchestrai.messaging.KafkaTopics#EXECUTIONS}. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionMessage {

    @JsonIgnore
    private Execution execution;

    public static ExecutionMessage from(Execution execution) {
        return new ExecutionMessage(execution);
    }

    public Execution toExecution() {
        return execution;
    }

    @JsonCreator
    public static ExecutionMessage deserialize(Execution execution) {
        return from(execution);
    }

    @JsonValue
    public Execution jsonValue() {
        return execution;
    }
}
