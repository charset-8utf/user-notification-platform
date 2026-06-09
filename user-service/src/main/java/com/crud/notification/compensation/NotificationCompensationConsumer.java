package com.crud.notification.compensation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Profile("kafka")
@RequiredArgsConstructor
@Slf4j
public class NotificationCompensationConsumer {

    private final NotificationCompensationMetrics metrics;

    @KafkaListener(
            topics = "${app.notification.kafka.compensation-topic}",
            groupId = "${app.notification.kafka.compensation-consumer-group:user-service-compensation}",
            containerFactory = "compensationKafkaListenerContainerFactory"
    )
    public void onCompensation(ConsumerRecord<String, NotificationCompensationEvent> consumerRecord) {
        NotificationCompensationEvent event = consumerRecord.value();
        if (event == null) {
            log.warn("Пустое compensating-событие: offset={}", consumerRecord.offset());
            return;
        }
        metrics.compensationReceived(event.originalOperation());
        log.warn(
                "Compensating saga step: delivery failed for originalEventId={}, operation={}, email={}, error={}, failedAt={}",
                event.originalEventId(),
                event.originalOperation(),
                event.email(),
                event.errorMessage(),
                event.failedAt());
    }
}
