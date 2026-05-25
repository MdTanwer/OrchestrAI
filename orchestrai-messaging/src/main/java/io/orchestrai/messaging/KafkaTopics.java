package io.orchestrai.messaging;

import java.util.Set;

/**
 * Kafka topic names for the OrchestrAI event bus.
 *
 * @see docs/05-architecture.md (Kafka Queue Topics &amp; Schema)
 */
public final class KafkaTopics {

    public static final String EXECUTIONS = "executions";
    public static final String TASK_RUNS = "task-runs";
    public static final String TASK_RESULTS = "task-results";
    public static final String TASK_LOGS = "task-logs";
    public static final String EXECUTION_EVENTS = "execution-events";
    public static final String DEAD_LETTER = "dead-letter";

    private static final Set<String> ALL = Set.of(
            EXECUTIONS,
            TASK_RUNS,
            TASK_RESULTS,
            TASK_LOGS,
            EXECUTION_EVENTS,
            DEAD_LETTER);

    private KafkaTopics() {
    }

    /** All topic names defined by this contract. */
    public static Set<String> all() {
        return ALL;
    }

    public static boolean isKnown(String topic) {
        return topic != null && ALL.contains(topic);
    }
}
