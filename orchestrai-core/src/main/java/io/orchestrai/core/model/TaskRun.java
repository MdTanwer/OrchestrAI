package io.orchestrai.core.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.orchestrai.core.enums.TaskRunState;
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
public class TaskRun {

    private UUID id;
    private UUID executionId;
    private String taskId;
    private String taskType;
    private TaskRunState state;

    @Builder.Default
    private int attempt = 1;

    @Builder.Default
    private Map<String, Object> inputs = new LinkedHashMap<>();

    @Builder.Default
    private Map<String, Object> outputs = new LinkedHashMap<>();

    private Instant startedAt;
    private Instant endedAt;
    private Long durationMs;
    private Integer tokensUsed;
    private BigDecimal costUsd;
    private String errorMessage;
    private String workerId;
    private String parentTaskId;
    private Instant createdAt;
}
