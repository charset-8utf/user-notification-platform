package com.crud.notification.compensation;

import com.crud.notification.UserNotificationOperation;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationCompensationMetricsTest {

    @Test
    void compensationReceived_incrementsCounterWithOperationTag() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        NotificationCompensationMetrics metrics = new NotificationCompensationMetrics(registry);

        metrics.compensationReceived(UserNotificationOperation.USER_DELETED);

        assertThat(registry.get("app.notification.compensation.received")
                .tag("operation", "USER_DELETED")
                .counter()
                .count()).isEqualTo(1.0);
    }
}
