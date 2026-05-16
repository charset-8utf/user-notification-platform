package com.crud.notification.outbox;

import com.crud.notification.UserNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Читает {@link OutboxStatus#PENDING} записи и публикует в Kafka (partition key = email).
 */
@Component
@Profile("kafka")
@Slf4j
@RequiredArgsConstructor
public class KafkaOutboxRelay {

    private final NotificationOutboxRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.notification.kafka.topic}")
    private String topic;

    @Value("${app.notification.kafka.outbox.batch-size:50}")
    private int batchSize;

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
            kafkaTemplate.send(topic, row.getEmail(), event).get();
            int updated = outboxRepository.markPublished(
                    row.getEventId(), OutboxStatus.PENDING, OutboxStatus.PUBLISHED, LocalDateTime.now());
            if (updated == 1) {
                log.info("Outbox → Kafka: eventId={}, operation={}, email={}, partitionKey={}",
                        row.getEventId(), row.getOperation(), row.getEmail(), row.getEmail());
            }
        } catch (Exception ex) {
            log.error("Не удалось опубликовать outbox eventId={}: {}", row.getEventId(), ex.toString());
            outboxRepository.markFailed(row.getEventId(), OutboxStatus.PENDING, OutboxStatus.FAILED);
        }
    }
}
