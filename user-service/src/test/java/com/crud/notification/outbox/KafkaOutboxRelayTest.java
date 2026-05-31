package com.crud.notification.outbox;

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
        ReflectionTestUtils.setField(relay, "batchSize", 10);
    }

    @Test
    void relayPendingEvents_doesNothingWhenQueueEmpty() throws Exception {
        when(outboxRepository.findByStatusOrderByCreatedAtAsc(eq(OutboxStatus.PENDING), any(PageRequest.class)))
                .thenReturn(List.of());

        relay.relayPendingEvents();

        verify(kafkaProducer, never()).send(any(), any());
    }

    @Test
    void relayPendingEvents_publishesAndRecordsMetric() throws Exception {
        UUID eventId = UUID.randomUUID();
        NotificationOutbox row = outboxRow(eventId, "ok@example.com");
        when(outboxRepository.findByStatusOrderByCreatedAtAsc(eq(OutboxStatus.PENDING), any(PageRequest.class)))
                .thenReturn(List.of(row));
        when(outboxRepository.markPublished(eq(eventId), eq(OutboxStatus.PENDING), eq(OutboxStatus.PUBLISHED), any(LocalDateTime.class)))
                .thenReturn(1);

        relay.relayPendingEvents();

        verify(kafkaProducer).send(any(UserNotificationEvent.class), eq("ok@example.com"));
        verify(outboxMetrics).recordPublished();
    }

    @Test
    void relayPendingEvents_skipsMetricWhenAlreadyPublished() throws Exception {
        UUID eventId = UUID.randomUUID();
        NotificationOutbox row = outboxRow(eventId, "race@example.com");
        when(outboxRepository.findByStatusOrderByCreatedAtAsc(eq(OutboxStatus.PENDING), any(PageRequest.class)))
                .thenReturn(List.of(row));
        when(outboxRepository.markPublished(eq(eventId), eq(OutboxStatus.PENDING), eq(OutboxStatus.PUBLISHED), any(LocalDateTime.class)))
                .thenReturn(0);

        relay.relayPendingEvents();

        verify(kafkaProducer).send(any(UserNotificationEvent.class), eq("race@example.com"));
        verify(outboxMetrics, never()).recordPublished();
    }

    @Test
    void relayPendingEvents_marksFailedWhenKafkaThrows() throws Exception {
        UUID eventId = UUID.randomUUID();
        NotificationOutbox row = outboxRow(eventId, "fail@example.com");
        when(outboxRepository.findByStatusOrderByCreatedAtAsc(eq(OutboxStatus.PENDING), any(PageRequest.class)))
                .thenReturn(List.of(row));
        doThrow(new RuntimeException("broker down")).when(kafkaProducer).send(any(), eq("fail@example.com"));
        when(outboxRepository.markFailed(eq(eventId), eq(OutboxStatus.PENDING), eq(OutboxStatus.FAILED)))
                .thenReturn(1);

        relay.relayPendingEvents();

        verify(outboxMetrics).recordFailed();
    }

    @Test
    void relayPendingEvents_skipsFailedMetricWhenAlreadyProcessed() throws Exception {
        UUID eventId = UUID.randomUUID();
        NotificationOutbox row = outboxRow(eventId, "stale@example.com");
        when(outboxRepository.findByStatusOrderByCreatedAtAsc(eq(OutboxStatus.PENDING), any(PageRequest.class)))
                .thenReturn(List.of(row));
        doThrow(new RuntimeException("broker down")).when(kafkaProducer).send(any(), eq("stale@example.com"));
        when(outboxRepository.markFailed(eq(eventId), eq(OutboxStatus.PENDING), eq(OutboxStatus.FAILED)))
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
