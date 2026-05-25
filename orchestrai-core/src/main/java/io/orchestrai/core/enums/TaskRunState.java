package io.orchestrai.core.enums;

/**
 * Lifecycle state of a single {@link io.orchestrai.core.model.TaskRun}.
 */
public enum TaskRunState {
    CREATED,
    RUNNING,
    SUCCESS,
    FAILED,
    CANCELLED;

    public boolean isTerminal() {
        return this == SUCCESS || this == FAILED || this == CANCELLED;
    }
}
