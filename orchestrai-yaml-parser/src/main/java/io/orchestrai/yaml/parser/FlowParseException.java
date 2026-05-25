package io.orchestrai.yaml.parser;

/**
 * Thrown when YAML cannot be parsed into a {@link io.orchestrai.core.model.Flow}.
 */
public class FlowParseException extends RuntimeException {

    public FlowParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
