package io.fnr.spring.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fnr")
public class FnrProperties {

    private boolean useVirtualThreads = true;
    private int     threadPoolSize    = 10;

    public boolean isUseVirtualThreads()            { return useVirtualThreads; }
    public void setUseVirtualThreads(boolean v)     { this.useVirtualThreads = v; }
    public int getThreadPoolSize()                  { return threadPoolSize; }
    public void setThreadPoolSize(int v)            { this.threadPoolSize = v; }
}
