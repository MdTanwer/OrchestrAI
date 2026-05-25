package io.orchestrai.core.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.orchestrai.core.enums.ExecutionState;
import io.orchestrai.core.enums.TriggerType;
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
public class Execution {

    private UUID id;
    private UUID flowId;
    private String namespace;
    private ExecutionState state;

    @Builder.Default
    private Map<String, Object> inputs = new LinkedHashMap<>();

    /**
     * Task outputs keyed by task id. Parallel parents store nested maps
     * (e.g. {@code outputs.check-services.server-a} → {@code Map} of child outputs).
     * JEXL resolves dot paths such as {@code outputs.check-services.server-a.body}.
     */
    @Builder.Default
    private Map<String, Object> outputs = new LinkedHashMap<>();

    private TriggerType triggerType;

    @Builder.Default
    private Map<String, Object> triggerData = new LinkedHashMap<>();

    @Builder.Default
    private List<TaskRun> taskRuns = new ArrayList<>();

    private Instant startedAt;
    private Instant endedAt;
    private Long durationMs;
    private BigDecimal totalCostUsd;
    private Long totalTokens;
    private String errorMessage;
    private Instant createdAt;

    public boolean hasTaskCompleted(String taskId) {
        if (taskRuns == null || taskId == null) {
            return false;
        }
        return taskRuns.stream()
                .anyMatch(tr -> taskId.equals(tr.getTaskId())
                        && tr.getState() != null
                        && tr.getState().isTerminal());
    }
}
