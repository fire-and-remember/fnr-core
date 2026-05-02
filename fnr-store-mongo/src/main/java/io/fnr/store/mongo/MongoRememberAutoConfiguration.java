package io.fnr.store.mongo;

import io.fnr.core.store.RememberStore;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;

@AutoConfiguration
@ConditionalOnClass(MongoTemplate.class)
public class MongoRememberAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(RememberStore.class)
    public RememberStore mongoRememberStore(MongoTemplate mongoTemplate) {
        return new MongoRememberStore(mongoTemplate);
    }
}
