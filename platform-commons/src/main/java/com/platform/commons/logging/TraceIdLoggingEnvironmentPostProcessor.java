package com.platform.commons.logging;

import org.jspecify.annotations.NullMarked;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.logging.LoggingSystemProperty;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

public class TraceIdLoggingEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String TRACE_MDC_PROPERTY = "platform.logging.trace-mdc";

    static final String TRACE_PATTERN =
            "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%X{traceId:-},%X{spanId:-}] %-5level %logger{36} - %msg%n";

    @Override
    @NullMarked
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!isTraceMdcEnabled(environment)) {
            return;
        }
        String key = LoggingSystemProperty.CONSOLE_PATTERN.getEnvironmentVariableName();
        if (System.getProperty(key) == null && environment.getProperty("logging.pattern.console") == null) {
            System.setProperty(key, TRACE_PATTERN);
        }
    }

    private static boolean isTraceMdcEnabled(ConfigurableEnvironment environment) {
        String value = environment.getProperty(TRACE_MDC_PROPERTY, "true");
        return !"false".equalsIgnoreCase(value);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}
