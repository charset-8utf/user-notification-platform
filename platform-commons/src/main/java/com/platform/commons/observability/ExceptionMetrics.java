package com.platform.commons.observability;

import io.micrometer.core.instrument.MeterRegistry;

public class ExceptionMetrics {

    private final MeterRegistry registry;

    public ExceptionMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordException(Throwable throwable, String source) {
        registry.counter(
                "app.errors.total",
                "exception",
                throwable.getClass().getSimpleName(),
                "source",
                source).increment();
    }

    public void recordHttpStatus(int status) {
        registry.counter("app.errors.total", "source", "http", "status", String.valueOf(status)).increment();
    }
}
