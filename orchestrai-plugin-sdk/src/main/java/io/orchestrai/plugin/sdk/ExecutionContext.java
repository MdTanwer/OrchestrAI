package io.orchestrai.plugin.sdk;

import java.util.Map;

/**
 * Runtime context passed to plugins at execute time.
 * Workers build this; plugins use {@link #getSecret(String)} and expression-backed maps.
 *
 * <p>Engine code must not define a duplicate type — use
 * {@link io.orchestrai.engine.context.EngineExecutionContext} to wrap this delegate.
 */
public class ExecutionContext {

    private final Map<String, Object> inputs;
    private final Map<String, Object> outputs;
    private final Map<String, Object> variables;

    public ExecutionContext(
            Map<String, Object> inputs,
            Map<String, Object> outputs,
            Map<String, Object> variables) {
        this.inputs = inputs;
        this.outputs = outputs;
        this.variables = variables;
    }

    public Map<String, Object> getInputs() {
        return inputs;
    }

    public Map<String, Object> getOutputs() {
        return outputs;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    /**
     * Resolve a secret by key (implementation injects vault/env in worker).
     */
    public String getSecret(String key) {
        throw new UnsupportedOperationException("getSecret not implemented yet: " + key);
    }
}
