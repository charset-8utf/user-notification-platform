package com.platform.commons.logging;

import org.springframework.core.env.ConfigurableEnvironment;

public class EnvironmentTraceMdcEnabledResolver implements TraceMdcEnabledResolver {

    static final String TRACE_MDC_PROPERTY = "platform.logging.trace-mdc";

    @Override
    public boolean isEnabled(ConfigurableEnvironment environment) {
        return environment.getProperty(TRACE_MDC_PROPERTY, Boolean.class, true);
    }
}
