package io.orchestrai.yaml.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregated validation errors and warnings from {@link FlowValidator}.
 */
public final class ValidationResult {

    private final List<String> errors;
    private final List<String> warnings;

    private ValidationResult(List<String> errors, List<String> warnings) {
        this.errors = List.copyOf(errors);
        this.warnings = List.copyOf(warnings);
    }

    public static ValidationResult ok() {
        return new ValidationResult(List.of(), List.of());
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public List<String> getErrors() {
        return errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public static final class Builder {

        private final List<String> errors = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();

        public Builder addError(String path, String message) {
            errors.add(path + ": " + message);
            return this;
        }

        public Builder addWarning(String path, String message) {
            warnings.add(path + ": " + message);
            return this;
        }

        public ValidationResult build() {
            return new ValidationResult(errors, warnings);
        }
    }
}
