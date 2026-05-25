package io.orchestrai.core.enums;

/**
 * How an {@link io.orchestrai.core.model.Execution} was started.
 */
public enum TriggerType {
    MANUAL,
    CRON,
    WEBHOOK,
    EVENT
}
