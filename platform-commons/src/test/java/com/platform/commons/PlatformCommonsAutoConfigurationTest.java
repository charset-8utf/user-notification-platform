package com.platform.commons;

import com.platform.commons.config.PlatformProperties;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.micrometer.metrics.autoconfigure.MetricsAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformCommonsAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    MetricsAutoConfiguration.class,
                    PlatformCommonsAutoConfiguration.class));

    @Test
    void registersPlatformPropertiesAndCommonTagsCustomizer() {
        contextRunner
                .withBean(MeterRegistry.class, SimpleMeterRegistry::new)
                .withPropertyValues(
                        "spring.application.name=demo-service",
                        "platform.environment=unit")
                .run(context -> {
                    assertThat(context).hasSingleBean(PlatformProperties.class);
                    assertThat(context).hasSingleBean(com.platform.commons.kafka.KafkaSecurityProperties.class);
                    assertThat(context).hasBean("platformCommonTags");
                });
    }
}
