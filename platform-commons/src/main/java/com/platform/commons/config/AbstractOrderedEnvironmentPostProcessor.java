package com.platform.commons.config;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Template Method: общий каркас {@link EnvironmentPostProcessor} с условным применением.
 */
public abstract class AbstractOrderedEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    @Override
    public final void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (shouldApply(environment)) {
            apply(environment, application);
        }
    }

    protected abstract boolean shouldApply(ConfigurableEnvironment environment);

    protected abstract void apply(ConfigurableEnvironment environment, SpringApplication application);
}
