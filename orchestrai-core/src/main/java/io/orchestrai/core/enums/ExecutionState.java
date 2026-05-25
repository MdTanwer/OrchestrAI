package io.orchestrai.core.enums;

/**
 * Lifecycle state of a flow {@link io.orchestrai.core.model.Execution}.
 */
public enum ExecutionState {
    CREATED,
    RUNNING,
    PAUSED,
    SUCCESS,
    FAILED,
    CANCELLED;

    public boolean isTerminal() {
        return this == SUCCESS || this == FAILED || this == CANCELLED;
    }
}
