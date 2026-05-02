package io.fnr.store.redis;

import io.fnr.core.store.RememberStore;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(RedissonClient.class)
@EnableConfigurationProperties(RedisStoreProperties.class)
public class RedisRememberAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(RememberStore.class)
    public RememberStore redisRememberStore(RedissonClient redissonClient,
                                            RedisStoreProperties props) {
        return new RedisRememberStore(redissonClient, props.getTtl());
    }
}
