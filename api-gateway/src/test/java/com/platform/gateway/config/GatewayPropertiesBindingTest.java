package com.platform.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayPropertiesBindingTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class);

    @Test
    void bindsGatewayApiPathsWithDefaults() {
        contextRunner.run(context -> {
            GatewayApiProperties properties = context.getBean(GatewayApiProperties.class);
            assertThat(properties.auth().login()).isEqualTo("/api/auth/login");
            assertThat(properties.user().users()).isEqualTo("/api/users/**");
            assertThat(properties.notification().logs()).isEqualTo("/api/notifications/logs/**");
        });
    }

    @Test
    void bindsGatewayRateLimitAndSsl() {
        contextRunner
                .withPropertyValues(
                        "app.gateway.rate-limit.auth.replenish-rate=10",
                        "app.gateway.ssl.insecure-trust-manager=false")
                .run(context -> {
                    assertThat(context.getBean(GatewayRateLimitProperties.class).auth().replenishRate()).isEqualTo(10);
                    assertThat(context.getBean(GatewaySslProperties.class).insecureTrustManager()).isFalse();
                });
    }

    @Configuration
    @EnableConfigurationProperties({
            GatewayApiProperties.class,
            GatewayRateLimitProperties.class,
            GatewaySslProperties.class,
            GatewayJwtProperties.class
    })
    static class TestConfig {
    }
}
