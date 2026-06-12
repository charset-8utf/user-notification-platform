package com.notification.kafka;

import com.notification.dto.NotificationEmailRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Сообщения из DLT → compensating event в {@code notification-compensations}.
 */
@Service
@Profile("kafka")
@RequiredArgsConstructor
@Slf4j
public class UserNotificationDltListener {

    private final NotificationCompensationPublisher compensationPublisher;
    private final DltExceptionMessageExtractor dltExceptionMessageExtractor;

    @KafkaListener(
            topics = "${app.notification.kafka.topic}${app.notification.kafka.dlt-suffix}",
            groupId = "${app.notification.kafka.dlt-listener-group:notification-service-dlt}",
            containerFactory = "dltKafkaListenerContainerFactory"
    )
    public void onDeadLetter(ConsumerRecord<String, NotificationEmailRequest> consumerRecord) {
        NotificationEmailRequest event = consumerRecord.value();
        String errorMessage = dltExceptionMessageExtractor.extract(consumerRecord);
        log.info(
                "DLT: eventId={}, operation={}, email={}, error={}",
                event.eventId(),
                event.operation(),
                event.email(),
                errorMessage);
        compensationPublisher.publishFromFailedDelivery(event, errorMessage);
    }
}
