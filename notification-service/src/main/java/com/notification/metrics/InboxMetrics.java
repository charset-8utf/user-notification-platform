package com.notification.metrics;

import com.notification.domain.InboxStatus;
import com.notification.repository.NotificationInboxRepository;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("kafka")
@RequiredArgsConstructor
public class InboxMetrics {

    private final NotificationInboxRepository inboxRepository;
    private final MeterRegistry meterRegistry;

    @PostConstruct
    void registerGauges() {
        meterRegistry.gauge("app.inbox.pending", this, InboxMetrics::pendingCount);
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
