package io.fnr.core.config;

/**
 * {@link FnrConfig} that executes tasks on a fixed-size thread pool.
 *
 * <p>Use this instead of {@link VirtualThreadFnrConfig} when you need to limit the
 * number of concurrently running tasks — for example, to protect a downstream database
 * or external API from being overwhelmed.
 *
 * <p>If all threads are busy, new tasks wait in a queue. The task is still persisted
 * as {@code PENDING} immediately upon submission, regardless of queue depth.
 *
 * <pre>{@code
 * @Bean
 * public FnrConfig fnrConfig() {
 *     return ThreadPoolFnrConfig.builder()
 *         .threadPoolSize(20)
 *         .storeResult(true)
 *         .build();
 * }
 * }</pre>
 */
public class ThreadPoolFnrConfig extends FnrConfig {

    private final int threadPoolSize;

    private ThreadPoolFnrConfig(Builder builder) {
        super(builder.storeParameters, builder.storeResult);
        this.threadPoolSize = builder.threadPoolSize;
    }

    /**
     * Returns the maximum number of tasks that can run concurrently.
     *
     * @return the thread pool size
     */
    public int getThreadPoolSize() { return threadPoolSize; }

    public static Builder builder() { return new Builder(); }

    /**
     * Builder for {@link ThreadPoolFnrConfig}.
     */
    public static class Builder {
        private boolean storeParameters = false;
        private boolean storeResult     = true;
        private int threadPoolSize      = 10;

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

        /**
         * Maximum number of tasks to execute concurrently. Defaults to {@code 10}.
         *
         * @param size the thread pool size; must be greater than 0
         * @return this builder
         */
        public Builder threadPoolSize(int size)          { this.threadPoolSize = size; return this; }

        /** @return a new {@link ThreadPoolFnrConfig} */
        public ThreadPoolFnrConfig build()        { return new ThreadPoolFnrConfig(this); }
    }
}
