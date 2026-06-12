package com.crud.notification.kafka;

import com.crud.config.kafka.UserNotificationKafkaProperties;
import com.crud.notification.UserNotificationEvent;
import com.crud.notification.UserNotificationOperation;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserNotificationKafkaProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private UserNotificationKafkaProducer producer;

    @BeforeEach
    void setUp() {
        UserNotificationKafkaProperties properties = new UserNotificationKafkaProperties(
                "user-notifications", 3, (short) 1, "notification-compensations",
                "user-service-compensation", new UserNotificationKafkaProperties.Outbox(1000, 50, 30000));
        producer = new UserNotificationKafkaProducer(kafkaTemplate, properties);
    }

    @Test
    void sendUsesTopicPartitionKeyAndEvent() throws Exception {
        UserNotificationEvent event = UserNotificationEvent.create(
                UserNotificationOperation.USER_CREATED, "user@example.com");
        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition("user-notifications", 1), 0, 42, 0, 0, 0);
        @SuppressWarnings("unchecked")
        SendResult<String, Object> sendResult = mock(SendResult.class);
        when(sendResult.getRecordMetadata()).thenReturn(metadata);
        when(kafkaTemplate.send("user-notifications", "user@example.com", event))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

        producer.send(event, "user@example.com");

        verify(kafkaTemplate).send("user-notifications", "user@example.com", event);
    }
}
