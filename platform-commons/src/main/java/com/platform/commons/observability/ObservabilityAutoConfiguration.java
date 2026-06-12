package com.platform.commons.observability;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.micrometer.metrics.autoconfigure.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

@AutoConfiguration
@ConditionalOnClass(MeterRegistry.class)
@Import(PlatformEnvironmentTagResolver.class)
public class ObservabilityAutoConfiguration {

    @Bean
    @ConditionalOnBean(MeterRegistry.class)
    MeterRegistryCustomizer<MeterRegistry> platformCommonTags(
            Environment environment,
            EnvironmentTagResolver environmentTagResolver) {
        String applicationName = environment.getProperty("spring.application.name", "unknown");
        String environmentTag = environmentTagResolver.resolve();
        return registry -> registry.config()
                .commonTags("application", applicationName, "environment", environmentTag);
    }
}
