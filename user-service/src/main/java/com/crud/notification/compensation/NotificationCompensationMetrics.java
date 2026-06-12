package com.crud.notification.compensation;

import com.crud.notification.UserNotificationOperation;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class NotificationCompensationMetrics {

    private static final String OPERATION_TAG = "operation";

    private final MeterRegistry registry;

    public NotificationCompensationMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void compensationReceived(UserNotificationOperation operation) {
        registry.counter("app.notification.compensation.received", OPERATION_TAG, operation.name()).increment();
    }

    public void compensationApplied(UserNotificationOperation operation, String action) {
        registry.counter(
                "app.notification.compensation.applied",
                OPERATION_TAG, operation.name(),
                "action", action
        ).increment();
    }

    public void compensationIdempotent(UserNotificationOperation operation) {
        registry.counter("app.notification.compensation.idempotent", OPERATION_TAG, operation.name()).increment();
    }
}
