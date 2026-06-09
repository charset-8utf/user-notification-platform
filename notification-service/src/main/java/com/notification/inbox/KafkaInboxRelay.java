package com.notification.inbox;

import com.notification.dto.NotificationEmailRequest;
import com.notification.kafka.NotificationCompensationPublisher;
import com.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Читает {@link InboxStatus#PENDING} записи inbox и вызывает доставку уведомления.
 */
@Component
@Profile("kafka")
@Slf4j
@RequiredArgsConstructor
public class KafkaInboxRelay {

    private final NotificationInboxRepository inboxRepository;
    private final NotificationService notificationService;
    private final NotificationInboxService inboxService;
    private final InboxMetrics inboxMetrics;
    private final NotificationCompensationPublisher compensationPublisher;

    @Value("${app.notification.kafka.inbox.batch-size:50}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${app.notification.kafka.inbox.failed-replay-interval-ms:30000}")
    public void replayFailedEvents() {
        List<NotificationInbox> failed = inboxRepository.findByStatusOrderByReceivedAtAsc(
                InboxStatus.FAILED, PageRequest.of(0, batchSize));
        for (NotificationInbox row : failed) {
            int requeued = inboxService.requeueFailed(UUID.fromString(row.getEventId()));
            if (requeued > 0) {
                log.info("Inbox eventId={} requeued from FAILED to PENDING", row.getEventId());
            }
        }
    }

    @Scheduled(fixedDelayString = "${app.notification.kafka.inbox.relay-interval-ms:1000}")
    public void relayPendingEvents() {
        List<NotificationInbox> pending = inboxRepository.findByStatusOrderByReceivedAtAsc(
                InboxStatus.PENDING, PageRequest.of(0, batchSize));
        for (NotificationInbox row : pending) {
            processOne(row);
        }
    }

    private void processOne(NotificationInbox row) {
        NotificationEmailRequest request = new NotificationEmailRequest(
                UUID.fromString(row.getEventId()),
                row.getOperation(),
                row.getEmail()
        );
        try {
            notificationService.sendEmailNotification(request);
            inboxMetrics.recordProcessed();
        } catch (Exception ex) {
            log.error("Не удалось обработать inbox eventId={}: {}", row.getEventId(), ex.toString());
            inboxService.markFailed(request.eventId());
            inboxMetrics.recordFailed();
            compensationPublisher.publishFromFailedDelivery(request, ex.getMessage());
        }
    }
}
