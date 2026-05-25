package io.orchestrai.messaging.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

import io.orchestrai.core.model.TaskResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Kafka payload for {@link io.orchestrai.messaging.KafkaTopics#TASK_RESULTS}. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskResultMessage {

    @JsonIgnore
    private TaskResult taskResult;

    public static TaskResultMessage from(TaskResult taskResult) {
        return new TaskResultMessage(taskResult);
    }

    public TaskResult toTaskResult() {
        return taskResult;
    }

    @JsonCreator
    public static TaskResultMessage deserialize(TaskResult taskResult) {
        return from(taskResult);
    }

    @JsonValue
    public TaskResult jsonValue() {
        return taskResult;
    }
}
