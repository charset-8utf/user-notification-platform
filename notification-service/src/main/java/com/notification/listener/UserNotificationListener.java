package com.notification.listener;

import com.notification.dto.NotificationEmailRequest;
import com.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Слушает события топика {@code user-notifications}.
 * Manual commit offset после успеха; идемпотентность по {@code eventId} (at-least-once).
 */
@Component
@Profile("kafka")
@RequiredArgsConstructor
@Slf4j
public class UserNotificationListener {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "${app.notification.kafka.topic}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onUserNotification(NotificationEmailRequest event, Acknowledgment acknowledgment) {
        log.info("Получено событие из Kafka: eventId={}, operation={}, email={}",
                event.eventId(), event.operation(), event.email());
        notificationService.sendEmailNotification(event);
        acknowledgment.acknowledge();
    }
}
