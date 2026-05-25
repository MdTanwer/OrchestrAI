package io.orchestrai.yaml.validation;

import java.util.Set;

/**
 * Built-in task and trigger type identifiers used by {@link FlowValidator}.
 * Extend via {@link FlowValidator#FlowValidator(java.util.Set, java.util.Set)} for custom plugins.
 */
public final class KnownPluginTypes {

    public static final Set<String> TASK_TYPES = Set.of(
            "openai.chat",
            "openai.moderation",
            "openai.embeddings",
            "anthropic.chat",
            "google.gemini",
            "ollama.chat",
            "http.request",
            "human.approval",
            "custom.logger",
            "core.parallel",
            "core.if",
            "core.foreach",
            "core.log",
            "postgres.query",
            "shell.exec",
            "kafka.publish");

    public static final Set<String> TRIGGER_TYPES = Set.of(
            "schedule.cron",
            "webhook",
            "kafka");

    private KnownPluginTypes() {
    }
}
