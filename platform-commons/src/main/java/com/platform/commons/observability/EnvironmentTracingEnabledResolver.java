package com.platform.commons.observability;

import org.springframework.core.env.ConfigurableEnvironment;

public class EnvironmentTracingEnabledResolver implements TracingEnabledResolver {

    static final String TRACING_ENABLED_ENV = "TRACING_ENABLED";
    static final String TRACING_ENABLED_PROPERTY = "platform.tracing.enabled";

    @Override
    public boolean isEnabled(ConfigurableEnvironment environment) {
        Boolean platform = environment.getProperty(TRACING_ENABLED_PROPERTY, Boolean.class);
        if (platform != null) {
            return platform;
        }
        return environment.getProperty(TRACING_ENABLED_ENV, Boolean.class, false);
    }
}
