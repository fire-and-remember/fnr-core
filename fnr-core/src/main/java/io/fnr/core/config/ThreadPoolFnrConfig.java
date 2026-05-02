package io.fnr.core.config;

public class ThreadPoolFnrConfig extends FnrConfig {

    private final int threadPoolSize;

    private ThreadPoolFnrConfig(Builder builder) {
        super(builder.storeParameters, builder.storeResult);
        this.threadPoolSize = builder.threadPoolSize;
    }

    public int getThreadPoolSize() { return threadPoolSize; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private boolean storeParameters = false;
        private boolean storeResult     = true;
        private int threadPoolSize      = 10;

        public Builder storeParameters(boolean enabled) { this.storeParameters = enabled; return this; }
        public Builder storeResult(boolean enabled)     { this.storeResult = enabled; return this; }
        public Builder threadPoolSize(int size)          { this.threadPoolSize = size; return this; }
        public ThreadPoolFnrConfig build()        { return new ThreadPoolFnrConfig(this); }
    }
}
