package com.notification.kafka;

import com.notification.dto.NotificationEmailRequest;
import com.notification.domain.UserNotificationOperation;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.record.TimestampType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.KafkaHeaders;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserNotificationDltListenerTest {

    @Mock
    private NotificationCompensationPublisher compensationPublisher;

    private final DltExceptionMessageExtractor dltExceptionMessageExtractor = new DltExceptionMessageExtractor();

    private UserNotificationDltListener listener;

    @BeforeEach
    void setUp() {
        listener = new UserNotificationDltListener(compensationPublisher, dltExceptionMessageExtractor);
    }

    @Test
    void onDeadLetter_publishesCompensationWithDltHeader() {
        NotificationEmailRequest request = new NotificationEmailRequest(
                UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
                UserNotificationOperation.USER_CREATED,
                "fail@example.com");
        RecordHeaders headers = new RecordHeaders();
        headers.add(new RecordHeader(
                KafkaHeaders.DLT_EXCEPTION_MESSAGE,
                "mail server down".getBytes(StandardCharsets.UTF_8)));
        ConsumerRecord<String, NotificationEmailRequest> consumerRecord = new ConsumerRecord<>(
                "user-notifications.DLT",
                0,
                1L,
                -1L,
                TimestampType.CREATE_TIME,
                -1,
                -1,
                request.eventId().toString(),
                request,
                headers,
                Optional.empty());

        listener.onDeadLetter(consumerRecord);

        verify(compensationPublisher).publishFromFailedDelivery(request, "mail server down");
    }

    @Test
    void onDeadLetter_withoutHeader_usesDefaultMessage() {
        NotificationEmailRequest request = NotificationEmailRequest.of(
                UserNotificationOperation.USER_DELETED, "x@example.com");
        ConsumerRecord<String, NotificationEmailRequest> consumerRecord = new ConsumerRecord<>(
                "user-notifications.DLT", 0, 2L, request.eventId().toString(), request);

        listener.onDeadLetter(consumerRecord);

        verify(compensationPublisher).publishFromFailedDelivery(
                request, "ошибка доставки (заголовок DLT exception отсутствует)");
    }
}
