package io.orchestrai.core.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.orchestrai.core.enums.ExecutionState;
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
public class ExecutionEvent {

    private UUID executionId;
    private ExecutionState state;
    private Instant timestamp;
    private BigDecimal totalCostUsd;
    private Long totalTokens;
    private Long durationMs;
    private String errorMessage;
}
