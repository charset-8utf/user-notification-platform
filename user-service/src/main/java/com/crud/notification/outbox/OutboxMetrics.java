package com.crud.notification.outbox;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("kafka")
public class OutboxMetrics {

    private final MeterRegistry registry;
    private final NotificationOutboxRepository outboxRepository;

    public OutboxMetrics(MeterRegistry registry, NotificationOutboxRepository outboxRepository) {
        this.registry = registry;
        this.outboxRepository = outboxRepository;
        registry.gauge("app.outbox.pending", this, OutboxMetrics::pendingCount);
    }

    public void recordPublished() {
        registry.counter("app.outbox.relay.published").increment();
    }

    public void recordFailed() {
        registry.counter("app.outbox.relay.failed").increment();
    }

    private double pendingCount() {
        return outboxRepository.countByStatus(OutboxStatus.PENDING);
    }
}
