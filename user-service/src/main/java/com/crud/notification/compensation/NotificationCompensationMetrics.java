package com.crud.notification.compensation;

import com.crud.notification.UserNotificationOperation;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class NotificationCompensationMetrics {

    private final MeterRegistry registry;

    public NotificationCompensationMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void compensationReceived(UserNotificationOperation operation) {
        registry.counter("app.notification.compensation.received", "operation", operation.name()).increment();
    }
}
