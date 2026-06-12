package com.crud.notification.outbox;

import com.crud.config.kafka.UserNotificationKafkaProperties;
import com.crud.notification.UserNotificationEvent;
import com.crud.notification.UserNotificationOperation;
import com.crud.notification.kafka.UserNotificationKafkaProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaOutboxRelayTest {

    private static final PageRequest PENDING_PAGE = PageRequest.of(0, 10);

    @Mock
    private NotificationOutboxRepository outboxRepository;
    @Mock
    private UserNotificationKafkaProducer kafkaProducer;
    @Mock
    private OutboxMetrics outboxMetrics;

    @InjectMocks
    private KafkaOutboxRelay relay;

    @BeforeEach
    void setUp() {
        UserNotificationKafkaProperties properties = new UserNotificationKafkaProperties(
                "user-notifications", 3, (short) 1, "notification-compensations",
                "user-service-compensation", new UserNotificationKafkaProperties.Outbox(1000, 10, 30000));
        ReflectionTestUtils.setField(relay, "notificationKafkaProperties", properties);
    }

    @Test
    void relayPendingEvents_doesNothingWhenQueueEmpty() throws Exception {
        when(outboxRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING, PENDING_PAGE))
                .thenReturn(List.of());

        relay.relayPendingEvents();

        verify(kafkaProducer, never()).send(any(), any());
    }

    @Test
    void relayPendingEvents_publishesAndRecordsMetric() throws Exception {
        UUID eventId = UUID.randomUUID();
        NotificationOutbox row = outboxRow(eventId, "ok@example.com");
        when(outboxRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING, PENDING_PAGE))
                .thenReturn(List.of(row));
        when(outboxRepository.markPublished(
                eq(eventId), eq(OutboxStatus.PENDING), eq(OutboxStatus.PUBLISHED), any(LocalDateTime.class)))
                .thenReturn(1);

        relay.relayPendingEvents();

        verify(kafkaProducer).send(
                new UserNotificationEvent(eventId, UserNotificationOperation.USER_CREATED, "ok@example.com"),
                "ok@example.com");
        verify(outboxMetrics).recordPublished();
    }

    @Test
    void relayPendingEvents_skipsMetricWhenAlreadyPublished() throws Exception {
        UUID eventId = UUID.randomUUID();
        NotificationOutbox row = outboxRow(eventId, "race@example.com");
        when(outboxRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING, PENDING_PAGE))
                .thenReturn(List.of(row));
        when(outboxRepository.markPublished(
                eq(eventId), eq(OutboxStatus.PENDING), eq(OutboxStatus.PUBLISHED), any(LocalDateTime.class)))
                .thenReturn(0);

        relay.relayPendingEvents();

        verify(kafkaProducer).send(
                new UserNotificationEvent(eventId, UserNotificationOperation.USER_CREATED, "race@example.com"),
                "race@example.com");
        verify(outboxMetrics, never()).recordPublished();
    }

    @Test
    void relayPendingEvents_marksFailedWhenKafkaThrows() throws Exception {
        UUID eventId = UUID.randomUUID();
        NotificationOutbox row = outboxRow(eventId, "fail@example.com");
        when(outboxRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING, PENDING_PAGE))
                .thenReturn(List.of(row));
        doThrow(new RuntimeException("broker down")).when(kafkaProducer).send(any(), any());
        when(outboxRepository.markFailed(eventId, OutboxStatus.PENDING, OutboxStatus.FAILED))
                .thenReturn(1);

        relay.relayPendingEvents();

        verify(outboxMetrics).recordFailed();
    }

    @Test
    void relayPendingEvents_skipsFailedMetricWhenAlreadyProcessed() throws Exception {
        UUID eventId = UUID.randomUUID();
        NotificationOutbox row = outboxRow(eventId, "stale@example.com");
        when(outboxRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING, PENDING_PAGE))
                .thenReturn(List.of(row));
        doThrow(new RuntimeException("broker down")).when(kafkaProducer).send(any(), any());
        when(outboxRepository.markFailed(eventId, OutboxStatus.PENDING, OutboxStatus.FAILED))
                .thenReturn(0);

        relay.relayPendingEvents();

        verify(outboxMetrics, never()).recordFailed();
    }

    private static NotificationOutbox outboxRow(UUID eventId, String email) {
        return NotificationOutbox.builder()
                .eventId(eventId)
                .operation(UserNotificationOperation.USER_CREATED)
                .email(email)
                .status(OutboxStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
