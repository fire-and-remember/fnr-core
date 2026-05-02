package io.fnr.store.jdbc;

import io.fnr.core.store.RememberStore;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@AutoConfiguration
@ConditionalOnClass(JdbcTemplate.class)
public class JdbcRememberAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JdbcStoreConfig jdbcStoreConfig() {
        return JdbcStoreConfig.defaults();
    }

    @Bean
    @ConditionalOnMissingBean(RememberStore.class)
    public RememberStore jdbcRememberStore(JdbcTemplate jdbcTemplate, JdbcStoreConfig jdbcStoreConfig) {
        return new JdbcRememberStore(jdbcTemplate, jdbcStoreConfig);
    }
}
