package com.notification.metrics;

import com.notification.domain.UserNotificationOperation;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationMetrics {

    private static final String OPERATION_TAG = "operation";

    private final MeterRegistry registry;

    public void emailSent(UserNotificationOperation operation) {
        registry.counter("app.notification.email.sent", OPERATION_TAG, operation.name()).increment();
    }

    public void emailFailed(UserNotificationOperation operation) {
        registry.counter("app.notification.email.failed", OPERATION_TAG, operation.name()).increment();
    }

    public void duplicateSkipped() {
        registry.counter("app.notification.duplicate.skipped").increment();
    }

    public void compensationPublished(UserNotificationOperation operation) {
        registry.counter("app.notification.compensation.published", OPERATION_TAG, operation.name()).increment();
    }
}
