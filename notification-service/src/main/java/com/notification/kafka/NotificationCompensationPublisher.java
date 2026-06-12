package com.notification.kafka;

import com.notification.config.kafka.NotificationKafkaProperties;
import com.notification.dto.NotificationCompensationEvent;
import com.notification.dto.NotificationEmailRequest;
import com.notification.metrics.NotificationMetrics;
import com.notification.service.ErrorMessageTruncator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@Profile("kafka")
@Slf4j
@RequiredArgsConstructor
public class NotificationCompensationPublisher {

    @Qualifier("compensationKafkaTemplate")
    private final KafkaTemplate<String, Object> compensationKafkaTemplate;
    private final NotificationMetrics notificationMetrics;
    private final NotificationKafkaProperties kafkaProperties;
    private final ErrorMessageTruncator errorMessageTruncator;

    public void publishFromFailedDelivery(NotificationEmailRequest original, @Nullable String errorMessage) {
        String compensationTopic = kafkaProperties.compensationTopic();
        NotificationCompensationEvent event = new NotificationCompensationEvent(
                original.eventId(),
                original.operation(),
                original.email(),
                errorMessageTruncator.truncate(errorMessage),
                Instant.now());
        compensationKafkaTemplate.send(compensationTopic, original.eventId().toString(), event);
        notificationMetrics.compensationPublished(original.operation());
        log.warn(
                "Опубликовано compensating-событие: topic={}, originalEventId={}, operation={}, email={}",
                compensationTopic,
                original.eventId(),
                original.operation(),
                original.email());
    }
}
