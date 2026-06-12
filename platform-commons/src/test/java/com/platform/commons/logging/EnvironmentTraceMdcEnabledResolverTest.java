package com.platform.commons.logging;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class EnvironmentTraceMdcEnabledResolverTest {

    private final EnvironmentTraceMdcEnabledResolver resolver = new EnvironmentTraceMdcEnabledResolver();

    @Test
    void enabledByDefault() {
        assertThat(resolver.isEnabled(new MockEnvironment())).isTrue();
    }

    @Test
    void respectsExplicitProperty() {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getSystemProperties().put(EnvironmentTraceMdcEnabledResolver.TRACE_MDC_PROPERTY, "false");

        assertThat(resolver.isEnabled(environment)).isFalse();
    }
}
