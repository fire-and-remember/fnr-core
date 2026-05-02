package io.fnr.core.config;

/**
 * {@link FnrConfig} that executes each task on a dedicated virtual thread.
 *
 * <p>This is the recommended configuration for most use cases. One virtual thread
 * is created per task, so there is no thread pool to tune and no risk of blocking
 * the carrier thread pool during I/O-heavy tasks.
 *
 * <p>Requires Java 21 or later.
 *
 * <pre>{@code
 * @Bean
 * public FnrConfig fnrConfig() {
 *     return VirtualThreadFnrConfig.builder()
 *         .storeResult(true)
 *         .storeParameters(false)
 *         .build();
 * }
 * }</pre>
 */
public class VirtualThreadFnrConfig extends FnrConfig {

    private VirtualThreadFnrConfig(Builder builder) {
        super(builder.storeParameters, builder.storeResult);
        if (Runtime.version().feature() < 21) {
            throw new IllegalStateException(
                "VirtualThreadFnrConfig requires Java 21 or later. Use ThreadPoolFnrConfig instead."
            );
        }
    }

    public static Builder builder() { return new Builder(); }

    /**
     * Builder for {@link VirtualThreadFnrConfig}.
     */
    public static class Builder {
        private boolean storeParameters = false;
        private boolean storeResult     = true;

        /**
         * Whether to persist method parameters as JSON. Defaults to {@code false}.
         *
         * @param enabled {@code true} to enable parameter storage
         * @return this builder
         */
        public Builder storeParameters(boolean enabled) { this.storeParameters = enabled; return this; }

        /**
         * Whether to persist the task result as JSON. Defaults to {@code true}.
         *
         * @param enabled {@code false} to disable result storage (status is still tracked)
         * @return this builder
         */
        public Builder storeResult(boolean enabled)     { this.storeResult = enabled; return this; }

        /** @return a new {@link VirtualThreadFnrConfig} */
        public VirtualThreadFnrConfig build()     { return new VirtualThreadFnrConfig(this); }
    }
}
