package com.crud.notification.compensation;

import com.crud.notification.UserNotificationOperation;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationCompensationConsumerTest {

    @Mock
    private NotificationCompensationHandler compensationHandler;

    @InjectMocks
    private NotificationCompensationConsumer consumer;

    @Test
    void onCompensation_ignoresNullPayload() {
        ConsumerRecord<String, NotificationCompensationEvent> consumerRecord =
                new ConsumerRecord<>("notification-compensations", 0, 42L, "key", null);

        consumer.onCompensation(consumerRecord);

        verify(compensationHandler, never()).handle(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void onCompensation_incrementsMetric() {
        NotificationCompensationEvent event = new NotificationCompensationEvent(
                UUID.randomUUID(),
                UserNotificationOperation.USER_CREATED,
                "u@example.com",
                "timeout",
                Instant.parse("2026-01-01T00:00:00Z"));
        ConsumerRecord<String, NotificationCompensationEvent> consumerRecord =
                new ConsumerRecord<>("notification-compensations", 0, 0L, "key", event);

        consumer.onCompensation(consumerRecord);

        verify(compensationHandler).handle(event);
    }
}
