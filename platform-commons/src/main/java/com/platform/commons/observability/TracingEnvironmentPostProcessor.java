package com.platform.commons.observability;

import com.platform.commons.config.AbstractOrderedEnvironmentPostProcessor;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Отключает Zipkin/tracing export, когда tracing не включён
 * ({@code platform.tracing.enabled=false} или {@code TRACING_ENABLED=false}).
 * <p>
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class TracingEnvironmentPostProcessor extends AbstractOrderedEnvironmentPostProcessor {

    private static final String EXCLUDE_KEY = "spring.autoconfigure.exclude";

    private final TracingEnabledResolver tracingEnabledResolver;
    private final ZipkinAutoConfigurationExcludeMerger zipkinExcludeMerger;

    public TracingEnvironmentPostProcessor() {
        this(new EnvironmentTracingEnabledResolver(), new PlatformZipkinAutoConfigurationExcludeMerger());
    }

    @Override
    protected boolean shouldApply(ConfigurableEnvironment environment) {
        return !tracingEnabledResolver.isEnabled(environment);
    }

    @Override
    protected void apply(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("management.tracing.enabled", false);
        properties.put(EXCLUDE_KEY, zipkinExcludeMerger.merge(environment.getProperty(EXCLUDE_KEY)));
        environment.getPropertySources().addFirst(new MapPropertySource("platformTracingDisabled", properties));
    }

    @Override
    public int getOrder() {
        return org.springframework.core.Ordered.HIGHEST_PRECEDENCE + 5;
    }
}
