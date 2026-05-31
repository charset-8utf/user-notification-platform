package com.crud.notification.outbox;

import com.crud.notification.UserNotificationEvent;
import com.crud.notification.support.AbstractUserNotificationPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Transactional outbox: событие сохраняется в БД в той же транзакции, что и пользователь;
 * публикация в Kafka выполняется отдельным {@link KafkaOutboxRelay}.
 */
@Component
@Profile("kafka")
@Slf4j
@RequiredArgsConstructor
public class OutboxUserNotificationPublisher extends AbstractUserNotificationPublisher {

    private final NotificationOutboxRepository outboxRepository;

    @Override
    @Transactional
    protected void doPublish(UserNotificationEvent event) {
        NotificationOutbox row = NotificationOutbox.builder()
                .eventId(event.eventId())
                .operation(event.operation())
                .email(event.email())
                .status(OutboxStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        outboxRepository.save(row);
        log.debug("Событие записано в outbox: eventId={}, operation={}, email={}",
                event.eventId(), event.operation(), event.email());
    }
}
