package com.platform.commons.observability;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionMetricsTest {

    @Test
    void recordsExceptionCounter() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        ExceptionMetrics metrics = new ExceptionMetrics(registry);

        metrics.recordException(new IllegalStateException("boom"), "service");

        assertThat(registry.find("app.errors.total")
                .tag("exception", "IllegalStateException")
                .tag("source", "service")
                .counter()).isNotNull();
    }
}
