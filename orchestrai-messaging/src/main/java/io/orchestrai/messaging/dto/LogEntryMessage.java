package io.orchestrai.messaging.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

import io.orchestrai.core.model.LogEntry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Kafka payload for {@link io.orchestrai.messaging.KafkaTopics#TASK_LOGS}. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogEntryMessage {

    @JsonIgnore
    private LogEntry logEntry;

    public static LogEntryMessage from(LogEntry logEntry) {
        return new LogEntryMessage(logEntry);
    }

    public LogEntry toLogEntry() {
        return logEntry;
    }

    @JsonCreator
    public static LogEntryMessage deserialize(LogEntry logEntry) {
        return from(logEntry);
    }

    @JsonValue
    public LogEntry jsonValue() {
        return logEntry;
    }
}
