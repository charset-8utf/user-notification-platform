package com.notification.kafka;

import com.notification.dto.NotificationCompensationEvent;
import com.notification.dto.NotificationEmailRequest;
import com.notification.metrics.NotificationMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@Profile("kafka")
@RequiredArgsConstructor
@Slf4j
public class NotificationCompensationPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final NotificationMetrics notificationMetrics;

    @Value("${app.notification.kafka.compensation-topic}")
    private String compensationTopic;

    public void publishFromFailedDelivery(NotificationEmailRequest original, String errorMessage) {
        NotificationCompensationEvent event = new NotificationCompensationEvent(
                original.eventId(),
                original.operation(),
                original.email(),
                truncate(errorMessage),
                Instant.now());
        kafkaTemplate.send(compensationTopic, original.eventId().toString(), event);
        notificationMetrics.compensationPublished(original.operation());
        log.warn(
                "Опубликовано compensating-событие: topic={}, originalEventId={}, operation={}, email={}",
                compensationTopic,
                original.eventId(),
                original.operation(),
                original.email());
    }

    private static String truncate(String message) {
        if (message == null) {
            return "unknown";
        }
        return message.length() <= 2000 ? message : message.substring(0, 2000);
    }
}
