package com.crud.notification.compensation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Profile("kafka")
@RequiredArgsConstructor
@Slf4j
public class NotificationCompensationConsumer {

    private final NotificationCompensationHandler compensationHandler;

    @KafkaListener(
            topics = "${app.notification.kafka.compensation-topic}",
            groupId = "${app.notification.kafka.compensation-consumer-group:user-service-compensation}",
            containerFactory = "compensationKafkaListenerContainerFactory"
    )
    public void onCompensation(ConsumerRecord<String, @Nullable NotificationCompensationEvent> consumerRecord) {
        NotificationCompensationEvent event = consumerRecord.value();
        if (event == null) {
            log.warn("Пустое compensating-событие: offset={}", consumerRecord.offset());
            return;
        }
        compensationHandler.handle(event);
    }
}
