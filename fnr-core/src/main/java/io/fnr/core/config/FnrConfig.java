package io.fnr.core.config;

/**
 * Base configuration for the FireAndRemember task executor.
 *
 * <p>Use {@link VirtualThreadFnrConfig} (recommended) or {@link ThreadPoolFnrConfig}
 * to create a concrete configuration and register it as a Spring bean.
 *
 * <p>If no {@code FnrConfig} bean is registered, {@link VirtualThreadFnrConfig}
 * with default settings is used automatically.
 *
 * <p>Result and parameter persistence are configured per-method via the
 * {@code storeResult} and {@code storeParameters} attributes of the
 * {@link io.fnr.spring.annotation.Remember} annotation.
 */
public abstract class FnrConfig {
    protected FnrConfig() {}
}
