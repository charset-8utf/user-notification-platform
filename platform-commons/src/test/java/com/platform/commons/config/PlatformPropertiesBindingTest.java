package com.platform.commons.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.StandardEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformPropertiesBindingTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withInitializer(PlatformPropertiesBindingTest::withoutSystemEnvironment)
            .withUserConfiguration(TestConfig.class);

    @Test
    void bindsPlatformPropertiesWithDefaults() {
        contextRunner.run(context -> {
            PlatformProperties properties = context.getBean(PlatformProperties.class);
            assertThat(properties.environment()).isEqualTo("default");
            assertThat(properties.logging().traceMdc()).isTrue();
            assertThat(properties.openapi().description()).isEqualTo("User Notification Platform API");
            assertThat(properties.openapi().version()).isEqualTo("1.0.0");
            assertThat(properties.tracing().enabled()).isFalse();
        });
    }

    private static void withoutSystemEnvironment(ConfigurableApplicationContext context) {
        context.getEnvironment().getPropertySources()
                .remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
    }

    @Test
    void bindsCustomPlatformProperties() {
        contextRunner
                .withPropertyValues(
                        "platform.environment=prod",
                        "platform.logging.trace-mdc=false",
                        "platform.openapi.version=3.1.0",
                        "platform.tracing.enabled=true")
                .run(context -> {
                    PlatformProperties properties = context.getBean(PlatformProperties.class);
                    assertThat(properties.environment()).isEqualTo("prod");
                    assertThat(properties.logging().traceMdc()).isFalse();
                    assertThat(properties.openapi().version()).isEqualTo("3.1.0");
                    assertThat(properties.tracing().enabled()).isTrue();
                });
    }

    @Configuration
    @EnableConfigurationProperties(PlatformProperties.class)
    static class TestConfig {
    }
}
