package io.fnr.store.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Duration;

@ConfigurationProperties(prefix = "fnr.store.redis")
public class RedisStoreProperties {
    private Duration ttl = null; // null = no TTL
    public Duration getTtl() { return ttl; }
    public void setTtl(Duration ttl) { this.ttl = ttl; }
}
