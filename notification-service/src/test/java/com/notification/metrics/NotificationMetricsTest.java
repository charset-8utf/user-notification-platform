package com.notification.metrics;

import com.notification.entity.UserNotificationOperation;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationMetricsTest {

    private SimpleMeterRegistry registry;
    private NotificationMetrics metrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        metrics = new NotificationMetrics(registry);
    }

    @Test
    void recordsEmailAndDuplicateMetrics() {
        metrics.emailSent(UserNotificationOperation.USER_CREATED);
        metrics.emailFailed(UserNotificationOperation.USER_DELETED);
        metrics.duplicateSkipped();

        assertThat(registry.get("app.notification.email.sent").tag("operation", "USER_CREATED").counter().count())
                .isEqualTo(1.0);
        assertThat(registry.get("app.notification.email.failed").tag("operation", "USER_DELETED").counter().count())
                .isEqualTo(1.0);
        assertThat(registry.get("app.notification.duplicate.skipped").counter().count()).isEqualTo(1.0);
    }
}
