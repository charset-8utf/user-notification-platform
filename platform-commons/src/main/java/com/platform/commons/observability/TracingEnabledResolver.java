package com.platform.commons.observability;

import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Strategy: определяет, включён ли distributed tracing.
 */
public interface TracingEnabledResolver {

    boolean isEnabled(ConfigurableEnvironment environment);
}
