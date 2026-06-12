package com.platform.commons.logging;

import com.platform.commons.config.AbstractOrderedEnvironmentPostProcessor;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.logging.LoggingSystemProperty;
import org.springframework.core.env.ConfigurableEnvironment;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class TraceIdLoggingEnvironmentPostProcessor extends AbstractOrderedEnvironmentPostProcessor {

    static final String TRACE_PATTERN =
            "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%X{traceId:-},%X{spanId:-}] %-5level %logger{36} - %msg%n";

    private final TraceMdcEnabledResolver traceMdcEnabledResolver;

    public TraceIdLoggingEnvironmentPostProcessor() {
        this(new EnvironmentTraceMdcEnabledResolver());
    }

    @Override
    protected boolean shouldApply(ConfigurableEnvironment environment) {
        return traceMdcEnabledResolver.isEnabled(environment);
    }

    @Override
    protected void apply(ConfigurableEnvironment environment, SpringApplication application) {
        String key = LoggingSystemProperty.CONSOLE_PATTERN.getEnvironmentVariableName();
        if (System.getProperty(key) == null && environment.getProperty("logging.pattern.console") == null) {
            System.setProperty(key, TRACE_PATTERN);
        }
    }

    @Override
    public int getOrder() {
        return org.springframework.core.Ordered.HIGHEST_PRECEDENCE + 10;
    }
}
