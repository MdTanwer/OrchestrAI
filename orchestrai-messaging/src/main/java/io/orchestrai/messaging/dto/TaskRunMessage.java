package io.orchestrai.messaging.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

import io.orchestrai.core.model.TaskRun;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kafka payload for {@link io.orchestrai.messaging.KafkaTopics#TASK_RUNS}
 * and {@link io.orchestrai.messaging.KafkaTopics#DEAD_LETTER} (same shape).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskRunMessage {

    @JsonIgnore
    private TaskRun taskRun;

    public static TaskRunMessage from(TaskRun taskRun) {
        return new TaskRunMessage(taskRun);
    }

    public TaskRun toTaskRun() {
        return taskRun;
    }

    @JsonCreator
    public static TaskRunMessage deserialize(TaskRun taskRun) {
        return from(taskRun);
    }

    @JsonValue
    public TaskRun jsonValue() {
        return taskRun;
    }
}
