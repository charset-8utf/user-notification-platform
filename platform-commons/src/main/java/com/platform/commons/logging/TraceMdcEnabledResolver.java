package com.platform.commons.logging;

import org.springframework.core.env.ConfigurableEnvironment;

public interface TraceMdcEnabledResolver {

    boolean isEnabled(ConfigurableEnvironment environment);
}
