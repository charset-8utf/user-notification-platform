package com.platform.commons.observability;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.StandardEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class TracingEnvironmentPostProcessorTest {

    private final TracingEnvironmentPostProcessor processor = new TracingEnvironmentPostProcessor();

    @Test
    void disablesTracingWhenPlatformPropertyIsFalse() {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getSystemProperties().put("platform.tracing.enabled", "false");

        processor.postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty("management.tracing.enabled")).isEqualTo("false");
        assertThat(environment.getProperty("spring.autoconfigure.exclude"))
                .contains("org.springframework.boot.zipkin.autoconfigure.ZipkinAutoConfiguration");
    }

    @Test
    void keepsTracingWhenPlatformPropertyIsTrue() {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getSystemProperties().put("platform.tracing.enabled", "true");

        processor.postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty("management.tracing.enabled")).isNull();
    }

    @Test
    void disablesTracingWhenLegacyTracingEnabledEnvIsFalse() {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getSystemProperties().put("TRACING_ENABLED", "false");

        processor.postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty("management.tracing.enabled")).isEqualTo("false");
    }

    @Test
    void appendsZipkinExcludeWhenOtherExcludesAlreadyConfigured() {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getSystemProperties().put("platform.tracing.enabled", "false");
        environment.getSystemProperties().put("spring.autoconfigure.exclude", "com.example.OtherAutoConfiguration");

        processor.postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty("spring.autoconfigure.exclude"))
                .contains("com.example.OtherAutoConfiguration")
                .contains("org.springframework.boot.zipkin.autoconfigure.ZipkinAutoConfiguration");
    }
}
