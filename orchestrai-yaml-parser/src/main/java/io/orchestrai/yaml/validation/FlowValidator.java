package io.orchestrai.yaml.validation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import io.orchestrai.core.model.Flow;
import io.orchestrai.core.model.Input;
import io.orchestrai.core.model.Task;
import io.orchestrai.core.model.Trigger;

/**
 * Static validation of parsed {@link Flow} models (required fields, ids, plugin types).
 */
public final class FlowValidator {

    private static final Pattern FLOW_ID = Pattern.compile("^[a-z0-9-]+$");
    private static final Pattern NAMESPACE = Pattern.compile("^[a-z0-9.-]+$");
    private static final Pattern TASK_ID = Pattern.compile("^[a-zA-Z0-9_-]+$");

    private final Set<String> knownTaskTypes;
    private final Set<String> knownTriggerTypes;

    public FlowValidator() {
        this(KnownPluginTypes.TASK_TYPES, KnownPluginTypes.TRIGGER_TYPES);
    }

    public FlowValidator(Set<String> knownTaskTypes, Set<String> knownTriggerTypes) {
        this.knownTaskTypes = Set.copyOf(knownTaskTypes);
        this.knownTriggerTypes = Set.copyOf(knownTriggerTypes);
    }

    public ValidationResult validate(Flow flow) {
        ValidationResult.Builder result = ValidationResult.builder();
        if (flow == null) {
            return result.addError("flow", "flow is null").build();
        }

        validateTopLevel(flow, result);
        validateInputs(flow.getInputs(), result);
        validateTriggers(flow.getTriggers(), result);
        validateTaskList(flow.getTasks(), "tasks", result);
        validateTaskList(flow.getOnFailure(), "onFailure", result);

        return result.build();
    }

    private void validateTopLevel(Flow flow, ValidationResult.Builder result) {
        if (flow.getFlowId() == null || flow.getFlowId().isBlank()) {
            result.addError("id", "flow id is required");
        } else if (!FLOW_ID.matcher(flow.getFlowId()).matches()) {
            result.addError("id", "flow id must match " + FLOW_ID.pattern());
        }

        if (flow.getNamespace() == null || flow.getNamespace().isBlank()) {
            result.addError("namespace", "namespace is required");
        } else if (!NAMESPACE.matcher(flow.getNamespace()).matches()) {
            result.addError("namespace", "namespace must match " + NAMESPACE.pattern());
        }

        if (flow.getTasks() == null || flow.getTasks().isEmpty()) {
            result.addError("tasks", "at least one task is required");
        }
    }

    private void validateInputs(List<Input> inputs, ValidationResult.Builder result) {
        if (inputs == null) {
            return;
        }
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < inputs.size(); i++) {
            Input input = inputs.get(i);
            String path = "inputs[" + i + "]";
            if (input.getId() == null || input.getId().isBlank()) {
                result.addError(path + ".id", "input id is required");
            } else if (!ids.add(input.getId())) {
                result.addError(path + ".id", "duplicate input id '" + input.getId() + "'");
            }
            if (input.getType() == null) {
                result.addError(path + ".type", "input type is required");
            }
        }
    }

    private void validateTriggers(List<Trigger> triggers, ValidationResult.Builder result) {
        if (triggers == null) {
            return;
        }
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < triggers.size(); i++) {
            Trigger trigger = triggers.get(i);
            String path = "triggers[" + i + "]";
            if (trigger.getId() == null || trigger.getId().isBlank()) {
                result.addError(path + ".id", "trigger id is required");
            } else if (!ids.add(trigger.getId())) {
                result.addError(path + ".id", "duplicate trigger id '" + trigger.getId() + "'");
            }
            if (trigger.getType() == null || trigger.getType().isBlank()) {
                result.addError(path + ".type", "trigger type is required");
            } else if (!knownTriggerTypes.contains(trigger.getType())) {
                result.addError(path + ".type", "unknown trigger type '" + trigger.getType() + "'");
            }
        }
    }

    private void validateTaskList(List<Task> tasks, String listPath, ValidationResult.Builder result) {
        if (tasks == null || tasks.isEmpty()) {
            return;
        }
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < tasks.size(); i++) {
            validateTask(tasks.get(i), listPath + "[" + i + "]", ids, result);
        }
    }

    private void validateTask(Task task, String path, Set<String> siblingIds, ValidationResult.Builder result) {
        if (task.getId() == null || task.getId().isBlank()) {
            result.addError(path + ".id", "task id is required");
        } else {
            if (!TASK_ID.matcher(task.getId()).matches()) {
                result.addError(path + ".id", "task id must match " + TASK_ID.pattern());
            }
            if (!siblingIds.add(task.getId())) {
                result.addError(path + ".id", "duplicate task id '" + task.getId() + "' in the same block");
            }
        }

        if (task.getType() == null || task.getType().isBlank()) {
            result.addError(path + ".type", "task type is required");
        } else if (!knownTaskTypes.contains(task.getType())) {
            result.addError(path + ".type", "unknown task type '" + task.getType() + "'");
        } else {
            validateControlFlowTask(task, path, result);
        }

        validateFallbackType(task, path, result);
    }

    private void validateControlFlowTask(Task task, String path, ValidationResult.Builder result) {
        switch (task.getType()) {
            case "core.if" -> {
                if (task.getCondition() == null || task.getCondition().isBlank()) {
                    result.addError(path + ".condition", "core.if requires condition");
                }
                if (task.getThen() == null || task.getThen().isEmpty()) {
                    result.addError(path + ".then", "core.if requires at least one then task");
                } else {
                    validateTaskList(task.getThen(), path + ".then", result);
                }
                validateTaskList(task.getElseTasks(), path + ".else", result);
            }
            case "core.parallel" -> {
                if (task.getTasks() == null || task.getTasks().isEmpty()) {
                    result.addError(path + ".tasks", "core.parallel requires nested tasks");
                } else {
                    validateTaskList(task.getTasks(), path + ".tasks", result);
                }
            }
            case "core.foreach" -> {
                if (task.getItems() == null || task.getItems().isBlank()) {
                    result.addError(path + ".items", "core.foreach requires items expression");
                }
                if (task.getTasks() == null || task.getTasks().isEmpty()) {
                    result.addError(path + ".tasks", "core.foreach requires nested tasks");
                } else {
                    validateTaskList(task.getTasks(), path + ".tasks", result);
                }
            }
            default -> { }
        }
    }

    private void validateFallbackType(Task task, String path, ValidationResult.Builder result) {
        Object fallback = task.getConfig() != null ? task.getConfig().get("fallback") : null;
        if (fallback instanceof java.util.Map<?, ?> map) {
            Object type = map.get("type");
            if (type instanceof String fallbackType) {
                if (!knownTaskTypes.contains(fallbackType)) {
                    result.addError(path + ".fallback.type", "unknown task type '" + fallbackType + "'");
                }
            } else {
                result.addError(path + ".fallback", "fallback requires type");
            }
        }
    }
}
