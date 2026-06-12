package com.notification.kafka;

import com.notification.config.kafka.NotificationKafkaProperties;
import com.notification.dto.NotificationEmailRequest;
import com.notification.entity.NotificationInbox;
import com.notification.metrics.InboxMetrics;
import com.notification.service.NotificationInboxService;
import com.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Infrastructure adapter: читает PENDING inbox и вызывает {@link NotificationService}.
 */
@Component
@Profile("kafka")
@Slf4j
@RequiredArgsConstructor
public class KafkaInboxRelay {

    private final NotificationInboxService inboxService;
    private final NotificationService notificationService;
    private final InboxMetrics inboxMetrics;
    private final NotificationCompensationPublisher compensationPublisher;
    private final NotificationKafkaProperties kafkaProperties;

    @Scheduled(fixedDelayString = "${app.notification.kafka.inbox.failed-replay-interval-ms:30000}")
    public void replayFailedEvents() {
        NotificationKafkaProperties.Inbox inbox = kafkaProperties.inbox();
        inboxService.requeueStaleProcessing(inbox.staleProcessingTimeoutMs());
        List<NotificationInbox> failed = inboxService.findFailedBatch(inbox.batchSize());
        for (NotificationInbox row : failed) {
            int required = inboxService.requeueFailed(UUID.fromString(row.getEventId()));
            if (required > 0) {
                log.info("Inbox eventId={} возвращён из FAILED в PENDING", row.getEventId());
            }
        }
    }

    @Scheduled(fixedDelayString = "${app.notification.kafka.inbox.relay-interval-ms:1000}")
    public void relayPendingEvents() {
        for (NotificationInbox row : inboxService.claimPendingBatch(kafkaProperties.inbox().batchSize())) {
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
