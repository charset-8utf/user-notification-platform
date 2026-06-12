package com.platform.bff.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class BffPropertiesBindingTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class);

    @Test
    void bindsClientPropertiesWithDefaults() {
        contextRunner.run(context -> {
            BffClientProperties properties = context.getBean(BffClientProperties.class);
            assertThat(properties.userServiceBaseUrl()).isEqualTo("https://user-service");
            assertThat(properties.notificationServiceBaseUrl()).isEqualTo("https://notification-service");
            assertThat(properties.loadBalanced()).isTrue();
            assertThat(properties.insecureSsl()).isFalse();
        });
    }

    @Test
    void bindsApiPathsWithDefaults() {
        contextRunner.run(context -> {
            BffApiProperties properties = context.getBean(BffApiProperties.class);
            assertThat(properties.actuator().health()).isEqualTo("/actuator/health");
            assertThat(properties.bff().authenticatedPath()).isEqualTo("/bff/**");
        });
    }

    @Test
    void bindsCustomClientAndApiValues() {
        contextRunner
                .withPropertyValues(
                        "app.bff.user-service-base-url=http://users:8081",
                        "app.bff.load-balanced=false",
                        "app.bff.api.bff.authenticated-path=/bff/v2/**")
                .run(context -> {
                    assertThat(context.getBean(BffClientProperties.class).userServiceBaseUrl())
                            .isEqualTo("http://users:8081");
                    assertThat(context.getBean(BffClientProperties.class).loadBalanced()).isFalse();
                    assertThat(context.getBean(BffApiProperties.class).bff().authenticatedPath())
                            .isEqualTo("/bff/v2/**");
                });
    }

    @Configuration
    @EnableConfigurationProperties({
            BffClientProperties.class,
            BffApiProperties.class,
            BffJwtProperties.class
    })
    static class TestConfig {
    }
}
