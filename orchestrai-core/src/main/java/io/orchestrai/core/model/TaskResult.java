package io.orchestrai.core.model;

import java.math.BigDecimal;
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
public class TaskResult {

    private UUID taskRunId;
    private UUID executionId;
    private String taskId;
    private TaskRunState state;

    @Builder.Default
    private Map<String, Object> outputs = new LinkedHashMap<>();

    private Integer tokensUsed;
    private BigDecimal costUsd;
    private Long durationMs;
    private String errorMessage;
    private String workerId;
}
