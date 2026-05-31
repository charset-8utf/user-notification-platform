package com.crud.notification.outbox;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxMetricsTest {

    @Mock
    private NotificationOutboxRepository outboxRepository;

    @Test
    void registersCountersAndPendingGauge() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        when(outboxRepository.countByStatus(OutboxStatus.PENDING)).thenReturn(4L);

        OutboxMetrics metrics = new OutboxMetrics(registry, outboxRepository);
        metrics.recordPublished();
        metrics.recordFailed();

        assertThat(registry.get("app.outbox.relay.published").counter().count()).isEqualTo(1.0);
        assertThat(registry.get("app.outbox.relay.failed").counter().count()).isEqualTo(1.0);
        assertThat(registry.get("app.outbox.pending").gauge().value()).isEqualTo(4.0);
    }
}
