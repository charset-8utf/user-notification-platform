package com.notification.kafka;

import com.notification.dto.NotificationEmailRequest;
import com.notification.inbox.NotificationInboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

/**
 * Принимает события из Kafka и сохраняет в transactional inbox;
 * доставка выполняется {@link com.notification.inbox.KafkaInboxRelay}.
 */
@Service
@Profile("kafka")
@RequiredArgsConstructor
@Slf4j
public class UserNotificationKafkaConsumer {

    private final NotificationInboxService inboxService;

    @KafkaListener(
            topics = "${app.notification.kafka.topic}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeUserNotification(
            ConsumerRecord<String, NotificationEmailRequest> consumerRecord,
            Acknowledgment acknowledgment
    ) {
        NotificationEmailRequest event = consumerRecord.value();
        log.info(
                "Получено из Kafka: topic={}, partition={}, offset={}, key={}, eventId={}, operation={}, email={}",
                consumerRecord.topic(),
                consumerRecord.partition(),
                consumerRecord.offset(),
                consumerRecord.key(),
                event.eventId(),
                event.operation(),
                event.email()
        );
        inboxService.enqueue(event);
        acknowledgment.acknowledge();
    }
}
