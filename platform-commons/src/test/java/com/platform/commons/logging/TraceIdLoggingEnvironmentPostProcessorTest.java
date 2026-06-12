package com.platform.commons.logging;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.logging.LoggingSystemProperty;
import org.springframework.core.env.StandardEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class TraceIdLoggingEnvironmentPostProcessorTest {

    private final TraceIdLoggingEnvironmentPostProcessor processor = new TraceIdLoggingEnvironmentPostProcessor();

    @Test
    void setsConsolePatternWhenTraceMdcEnabled() {
        String key = LoggingSystemProperty.CONSOLE_PATTERN.getEnvironmentVariableName();
        System.clearProperty(key);

        StandardEnvironment environment = new StandardEnvironment();
        environment.getSystemProperties().put(EnvironmentTraceMdcEnabledResolver.TRACE_MDC_PROPERTY, "true");

        processor.postProcessEnvironment(environment, new SpringApplication());

        assertThat(System.getProperty(key)).isEqualTo(TraceIdLoggingEnvironmentPostProcessor.TRACE_PATTERN);
        System.clearProperty(key);
    }

    @Test
    void skipsWhenTraceMdcDisabled() {
        String key = LoggingSystemProperty.CONSOLE_PATTERN.getEnvironmentVariableName();
        System.clearProperty(key);

        StandardEnvironment environment = new StandardEnvironment();
        environment.getSystemProperties().put(EnvironmentTraceMdcEnabledResolver.TRACE_MDC_PROPERTY, "false");

        processor.postProcessEnvironment(environment, new SpringApplication());

        assertThat(System.getProperty(key)).isNull();
    }
}
