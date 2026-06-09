package com.crud.notification.outbox;

import com.crud.notification.UserNotificationEvent;
import com.crud.notification.kafka.UserNotificationKafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Читает {@link OutboxStatus#PENDING} записи и публикует через {@link UserNotificationKafkaProducer}.
 */
@Component
@Profile("kafka")
@Slf4j
@RequiredArgsConstructor
public class KafkaOutboxRelay {

    private final NotificationOutboxRepository outboxRepository;
    private final UserNotificationKafkaProducer kafkaProducer;
    private final OutboxMetrics outboxMetrics;

    @Value("${app.notification.kafka.outbox.batch-size:50}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${app.notification.kafka.outbox.failed-replay-interval-ms:30000}")
    @Transactional
    public void replayFailedEvents() {
        List<NotificationOutbox> failed = outboxRepository.findByStatusOrderByCreatedAtAsc(
                OutboxStatus.FAILED, PageRequest.of(0, batchSize));
        for (NotificationOutbox row : failed) {
            int requeued = outboxRepository.requeueFailed(row.getEventId(), OutboxStatus.FAILED, OutboxStatus.PENDING);
            if (requeued > 0) {
                log.info("Outbox eventId={} requeued from FAILED to PENDING", row.getEventId());
            }
        }
    }

    @Scheduled(fixedDelayString = "${app.notification.kafka.outbox.relay-interval-ms:1000}")
    @Transactional
    public void relayPendingEvents() {
        List<NotificationOutbox> pending = outboxRepository.findByStatusOrderByCreatedAtAsc(
                OutboxStatus.PENDING, PageRequest.of(0, batchSize));
        if (pending.isEmpty()) {
            return;
        }
        for (NotificationOutbox row : pending) {
            publishOne(row);
        }
    }

    private void publishOne(NotificationOutbox row) {
        UserNotificationEvent event = new UserNotificationEvent(row.getEventId(), row.getOperation(), row.getEmail());
        try {
            kafkaProducer.send(event, row.getEmail());
            int updated = outboxRepository.markPublished(
                    row.getEventId(), OutboxStatus.PENDING, OutboxStatus.PUBLISHED, LocalDateTime.now());
            if (updated == 0) {
                log.debug("Outbox eventId={} уже обработан другим relay", row.getEventId());
            } else {
                outboxMetrics.recordPublished();
            }
        } catch (Exception ex) {
            log.error("Не удалось опубликовать outbox eventId={}: {}", row.getEventId(), ex.toString());
            int failed = outboxRepository.markFailed(row.getEventId(), OutboxStatus.PENDING, OutboxStatus.FAILED);
            if (failed == 0) {
                log.debug("Outbox eventId={} не помечен FAILED (уже обработан другим relay)", row.getEventId());
            } else {
                outboxMetrics.recordFailed();
            }
        }
    }
}
