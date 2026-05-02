package io.fnr.core.config;

/**
 * Base configuration for the FireAndRemember task executor.
 *
 * <p>Use {@link VirtualThreadFnrConfig} (recommended) or {@link ThreadPoolFnrConfig}
 * to create a concrete configuration and register it as a Spring bean.
 *
 * <p>If no {@code FnrConfig} bean is registered, {@link VirtualThreadFnrConfig}
 * with default settings is used automatically.
 */
public abstract class FnrConfig {
    private final boolean storeParameters;
    private final boolean storeResult;

    protected FnrConfig(boolean storeParameters, boolean storeResult) {
        this.storeParameters = storeParameters;
        this.storeResult     = storeResult;
    }

    /**
     * Returns whether the method parameters are serialized and persisted with the task record.
     *
     * <p>When {@code true}, the original method arguments are JSON-serialized and stored.
     * They can be retrieved via {@link io.fnr.core.domain.TicketResult#getParamPayload()}.
     * Defaults to {@code false}.
     *
     * @return {@code true} if parameter persistence is enabled
     */
    public boolean isStoreParameters() { return storeParameters; }

    /**
     * Returns whether the task return value is serialized and persisted.
     *
     * <p>When {@code true}, the result is JSON-serialized and can be retrieved via
     * {@link io.fnr.core.domain.TicketResult#getValue()}. When {@code false},
     * {@code getValue()} always returns {@code null}, but the status is still tracked.
     * Defaults to {@code true}.
     *
     * @return {@code true} if result persistence is enabled
     */
    public boolean isStoreResult()     { return storeResult; }
}
