package com.platform.commons.observability;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExceptionMetrics {

    private final MeterRegistry registry;

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
