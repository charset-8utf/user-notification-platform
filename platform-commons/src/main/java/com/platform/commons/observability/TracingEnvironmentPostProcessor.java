package com.platform.commons.observability;

import org.jspecify.annotations.NullMarked;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Отключает Zipkin/tracing export, когда {@code TRACING_ENABLED=false}
 * (профиль observability не активен).
 */
public class TracingEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String TRACING_ENABLED = "TRACING_ENABLED";
    private static final String EXCLUDE_KEY = "spring.autoconfigure.exclude";
    private static final String ZIPKIN_AUTO_CONFIG =
            "org.springframework.boot.zipkin.autoconfigure.ZipkinAutoConfiguration";

    @Override
    @NullMarked
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (isTracingEnabled(environment)) {
            return;
        }
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("management.tracing.enabled", false);
        properties.put(EXCLUDE_KEY, mergeExclude(environment.getProperty(EXCLUDE_KEY), ZIPKIN_AUTO_CONFIG));
        environment.getPropertySources().addFirst(new MapPropertySource("platformTracingDisabled", properties));
    }

    private static boolean isTracingEnabled(ConfigurableEnvironment environment) {
        return environment.getProperty(TRACING_ENABLED, Boolean.class, false);
    }

    private static String mergeExclude(String existing, String className) {
        if (existing == null || existing.isBlank()) {
            return className;
        }
        if (existing.contains(className)) {
            return existing;
        }
        return existing + "," + className;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 5;
    }
}
