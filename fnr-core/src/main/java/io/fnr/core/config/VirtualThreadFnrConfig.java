package io.fnr.core.config;

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

    public static class Builder {
        private boolean storeParameters = false;
        private boolean storeResult     = true;

        public Builder storeParameters(boolean enabled) { this.storeParameters = enabled; return this; }
        public Builder storeResult(boolean enabled)     { this.storeResult = enabled; return this; }
        public VirtualThreadFnrConfig build()     { return new VirtualThreadFnrConfig(this); }
    }
}
