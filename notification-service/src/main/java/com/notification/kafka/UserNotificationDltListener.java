package com.notification.kafka;

import com.notification.dto.NotificationEmailRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * Сообщения из DLT → compensating event в {@code notification-compensations}.
 */
@Service
@Profile("kafka")
@RequiredArgsConstructor
@Slf4j
public class UserNotificationDltListener {

    private final NotificationCompensationPublisher compensationPublisher;

    @KafkaListener(
            topics = "${app.notification.kafka.topic}${app.notification.kafka.dlt-suffix}",
            groupId = "${app.notification.kafka.dlt-listener-group:notification-service-dlt}",
            containerFactory = "dltKafkaListenerContainerFactory"
    )
    public void onDeadLetter(ConsumerRecord<String, NotificationEmailRequest> consumerRecord) {
        NotificationEmailRequest event = consumerRecord.value();
        if (event == null) {
            log.error("DLT record без payload: topic={}, offset={}", consumerRecord.topic(), consumerRecord.offset());
            return;
        }
        String errorMessage = extractDltExceptionMessage(consumerRecord);
        log.info(
                "DLT: eventId={}, operation={}, email={}, error={}",
                event.eventId(),
                event.operation(),
                event.email(),
                errorMessage);
        compensationPublisher.publishFromFailedDelivery(event, errorMessage);
    }

    private static String extractDltExceptionMessage(ConsumerRecord<String, NotificationEmailRequest> consumerRecord) {
        Header header = consumerRecord.headers().lastHeader(KafkaHeaders.DLT_EXCEPTION_MESSAGE);
        if (header == null || header.value() == null) {
            return "delivery failed (no DLT exception header)";
        }
        return new String(header.value(), StandardCharsets.UTF_8);
    }
}
