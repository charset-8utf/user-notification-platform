package com.platform.commons.observability;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class EnvironmentTracingEnabledResolverTest {

    private final EnvironmentTracingEnabledResolver resolver = new EnvironmentTracingEnabledResolver();

    @Test
    void usesPlatformPropertyWhenPresent() {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getSystemProperties().put(EnvironmentTracingEnabledResolver.TRACING_ENABLED_PROPERTY, "true");

        assertThat(resolver.isEnabled(environment)).isTrue();
    }

    @Test
    void fallsBackToLegacyEnvVariable() {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getSystemProperties().put(EnvironmentTracingEnabledResolver.TRACING_ENABLED_ENV, "true");

        assertThat(resolver.isEnabled(environment)).isTrue();
    }

    @Test
    void defaultsToDisabled() {
        assertThat(resolver.isEnabled(new MockEnvironment())).isFalse();
    }
}
