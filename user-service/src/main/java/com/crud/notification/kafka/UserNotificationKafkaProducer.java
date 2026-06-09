package com.crud.notification.kafka;

import com.crud.notification.UserNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;


@Service
@Profile("kafka")
@Slf4j
@RequiredArgsConstructor
public class UserNotificationKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.notification.kafka.topic}")
    private String topic;

    public void send(UserNotificationEvent event, String partitionKey) throws Exception {
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(topic, partitionKey, event);
        RecordMetadata metadata = future.get().getRecordMetadata();
        log.info(
                "Событие отправлено в Kafka: topic={}, partition={}, offset={}, key={}, eventId={}, operation={}",
                metadata.topic(),
                metadata.partition(),
                metadata.offset(),
                partitionKey,
                event.eventId(),
                event.operation()
        );
    }
}
