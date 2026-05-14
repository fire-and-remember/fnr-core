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
 *     return VirtualThreadFnrConfig.builder().build();
 * }
 * }</pre>
 */
public class VirtualThreadFnrConfig extends FnrConfig {

    private VirtualThreadFnrConfig() {
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
        /** @return a new {@link VirtualThreadFnrConfig} */
        public VirtualThreadFnrConfig build() { return new VirtualThreadFnrConfig(); }
    }
}
