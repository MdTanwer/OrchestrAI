package io.orchestrai.core.model;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.orchestrai.core.enums.LogLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LogEntry {

    private Long id;
    private UUID executionId;
    private UUID taskRunId;
    private LogLevel level;
    private String message;

    @Builder.Default
    private Map<String, Object> metadata = new LinkedHashMap<>();

    private Instant createdAt;
}
