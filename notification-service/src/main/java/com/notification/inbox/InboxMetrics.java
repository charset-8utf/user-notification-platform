package com.notification.inbox;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("kafka")
public class InboxMetrics {

    private final NotificationInboxRepository inboxRepository;
    private final MeterRegistry meterRegistry;

    public InboxMetrics(MeterRegistry registry, NotificationInboxRepository inboxRepository) {
        this.meterRegistry = registry;
        this.inboxRepository = inboxRepository;
        registry.gauge("app.inbox.pending", this, InboxMetrics::pendingCount);
    }

    public void recordProcessed() {
        meterRegistry.counter("app.inbox.relay.processed").increment();
    }

    public void recordFailed() {
        meterRegistry.counter("app.inbox.relay.failed").increment();
    }

    private double pendingCount() {
        return inboxRepository.countByStatus(InboxStatus.PENDING);
    }
}
