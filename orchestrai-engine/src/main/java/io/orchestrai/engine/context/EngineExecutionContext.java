package io.orchestrai.engine.context;

import io.orchestrai.plugin.sdk.ExecutionContext;

/**
 * Engine-facing wrapper around {@link ExecutionContext} from the plugin SDK.
 * Keeps a single context type for plugins while allowing engine-only helpers later.
 */
public final class EngineExecutionContext {

    private final ExecutionContext delegate;

    public EngineExecutionContext(ExecutionContext delegate) {
        this.delegate = delegate;
    }

    public static EngineExecutionContext wrap(ExecutionContext delegate) {
        return new EngineExecutionContext(delegate);
    }

    public ExecutionContext getDelegate() {
        return delegate;
    }
}
