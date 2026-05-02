package io.fnr.core.config;

public abstract class FnrConfig {
    private final boolean storeParameters;
    private final boolean storeResult;

    protected FnrConfig(boolean storeParameters, boolean storeResult) {
        this.storeParameters = storeParameters;
        this.storeResult     = storeResult;
    }

    public boolean isStoreParameters() { return storeParameters; }
    public boolean isStoreResult()     { return storeResult; }
}
