package com.platform.commons.observability;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.micrometer.metrics.autoconfigure.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.micrometer.metrics.autoconfigure.MetricsAutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@AutoConfigureAfter({
        MetricsAutoConfiguration.class,
        CompositeMeterRegistryAutoConfiguration.class
})
@ConditionalOnBean(MeterRegistry.class)
public class ExceptionMetricsAutoConfiguration {

    @Bean
    ExceptionMetrics exceptionMetrics(MeterRegistry registry) {
        return new ExceptionMetrics(registry);
    }
}
