package com.notification.metrics;

import com.notification.entity.UserNotificationOperation;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class NotificationMetrics {

    private final MeterRegistry registry;

    public NotificationMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void emailSent(UserNotificationOperation operation) {
        registry.counter("app.notification.email.sent", "operation", operation.name()).increment();
    }

    public void emailFailed(UserNotificationOperation operation) {
        registry.counter("app.notification.email.failed", "operation", operation.name()).increment();
    }

    public void duplicateSkipped() {
        registry.counter("app.notification.duplicate.skipped").increment();
    }
}
