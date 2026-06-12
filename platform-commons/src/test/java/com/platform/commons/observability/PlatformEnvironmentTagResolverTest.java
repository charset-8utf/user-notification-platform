package com.platform.commons.observability;

import com.platform.commons.config.PlatformProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformEnvironmentTagResolverTest {

    @Test
    void usesConfiguredEnvironmentWhenExplicitlySet() {
        PlatformProperties properties = propertiesWithEnvironment("staging");
        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.profiles.active", "integration");

        PlatformEnvironmentTagResolver resolver = new PlatformEnvironmentTagResolver(properties, environment);

        assertThat(resolver.resolve()).isEqualTo("staging");
    }

    @Test
    void fallsBackToActiveProfileWhenEnvironmentIsDefault() {
        PlatformProperties properties = propertiesWithEnvironment("default");
        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.profiles.active", "cloud");

        PlatformEnvironmentTagResolver resolver = new PlatformEnvironmentTagResolver(properties, environment);

        assertThat(resolver.resolve()).isEqualTo("cloud");
    }

    private static PlatformProperties propertiesWithEnvironment(String environment) {
        return new PlatformProperties(
                environment,
                new PlatformProperties.Logging(true),
                new PlatformProperties.OpenApi("User Notification Platform API", "1.0.0"),
                new PlatformProperties.Tracing(false));
    }
}
