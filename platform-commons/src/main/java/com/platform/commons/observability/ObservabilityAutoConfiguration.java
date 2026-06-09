package com.platform.commons.observability;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.micrometer.metrics.autoconfigure.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(MeterRegistry.class)
public class ObservabilityAutoConfiguration {

    @Bean
    @ConditionalOnBean(MeterRegistry.class)
    MeterRegistryCustomizer<MeterRegistry> platformCommonTags(
            @Value("${spring.application.name:unknown}") String applicationName,
            @Value("${platform.environment:${spring.profiles.active:default}}") String environment) {
        return registry -> registry.config()
                .commonTags("application", applicationName, "environment", environment);
    }
}
