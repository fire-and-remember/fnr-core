package io.fnr.spring.config;

import io.fnr.core.config.FnrConfig;
import io.fnr.core.config.ThreadPoolFnrConfig;
import io.fnr.core.config.VirtualThreadFnrConfig;
import io.fnr.core.executor.DefaultRememberExecutor;
import io.fnr.core.executor.RememberExecutor;
import io.fnr.core.service.FnrTicketService;
import io.fnr.core.store.RememberStore;
import io.fnr.spring.aspect.RememberAspect;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(FnrProperties.class)
public class FnrAutoConfiguration implements InitializingBean, DisposableBean {

    private final ApplicationContext applicationContext;
    private DefaultRememberExecutor executorInstance;

    public FnrAutoConfiguration(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    @ConditionalOnMissingBean
    public FnrConfig fnrConfig(FnrProperties props) {
        if (!props.isUseVirtualThreads()) {
            return ThreadPoolFnrConfig.builder()
                .threadPoolSize(props.getThreadPoolSize())
                .build();
        }
        return VirtualThreadFnrConfig.builder().build();
    }

    @Bean
    @ConditionalOnMissingBean
    public RememberExecutor rememberExecutor(RememberStore store, FnrConfig config) {
        executorInstance = new DefaultRememberExecutor(store, config);
        return executorInstance;
    }

    @Bean
    public FnrTicketService fnrTicketService(RememberExecutor executor) {
        return executor;
    }

    @Bean
    public RememberAspect rememberAspect(RememberExecutor executor) {
        return new RememberAspect(executor);
    }

    @Override
    public void afterPropertiesSet() {
        if (applicationContext.getBeanNamesForType(RememberStore.class).length == 0) {
            throw new IllegalStateException(
                "No RememberStore bean found. Add a store module to your dependencies: fnr-store-jdbc or fnr-store-redis."
            );
        }
    }

    @Override
    public void destroy() {
        if (executorInstance != null) executorInstance.shutdown();
    }
}
