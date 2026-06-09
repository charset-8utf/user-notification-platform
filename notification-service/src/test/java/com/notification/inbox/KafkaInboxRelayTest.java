package com.notification.inbox;

import com.notification.dto.NotificationEmailRequest;
import com.notification.entity.UserNotificationOperation;
import com.notification.kafka.NotificationCompensationPublisher;
import com.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaInboxRelayTest {

    @Mock
    private NotificationInboxRepository inboxRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationInboxService inboxService;

    @Mock
    private InboxMetrics inboxMetrics;

    @Mock
    private NotificationCompensationPublisher compensationPublisher;

    @InjectMocks
    private KafkaInboxRelay relay;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(relay, "batchSize", 50);
    }

    @Test
    void relayPendingProcessesEvent() {
        UUID eventId = UUID.randomUUID();
        NotificationInbox row = NotificationInbox.pending(
                eventId, UserNotificationOperation.USER_CREATED, "ok@example.com");
        when(inboxRepository.findByStatusOrderByReceivedAtAsc(eq(InboxStatus.PENDING), any(PageRequest.class)))
                .thenReturn(List.of(row));

        relay.relayPendingEvents();

        verify(notificationService).sendEmailNotification(any(NotificationEmailRequest.class));
        verify(inboxMetrics).recordProcessed();
        verify(inboxService, never()).markFailed(any());
    }

    @Test
    void relayPendingMarksFailedOnDeliveryError() {
        UUID eventId = UUID.randomUUID();
        NotificationInbox row = NotificationInbox.pending(
                eventId, UserNotificationOperation.USER_DELETED, "fail@example.com");
        when(inboxRepository.findByStatusOrderByReceivedAtAsc(eq(InboxStatus.PENDING), any(PageRequest.class)))
                .thenReturn(List.of(row));
        doThrow(new RuntimeException("smtp down")).when(notificationService).sendEmailNotification(any());

        relay.relayPendingEvents();

        verify(inboxService).markFailed(eventId);
        verify(inboxMetrics).recordFailed();
        verify(compensationPublisher).publishFromFailedDelivery(any(NotificationEmailRequest.class), eq("smtp down"));
    }

    @Test
    void replayFailedRequeuesRows() {
        UUID eventId = UUID.randomUUID();
        NotificationInbox row = NotificationInbox.pending(
                eventId, UserNotificationOperation.USER_CREATED, "retry@example.com");
        row = new NotificationInbox(
                row.getEventId(), row.getOperation(), row.getEmail(),
                InboxStatus.FAILED, row.getReceivedAt(), row.getReceivedAt());
        when(inboxRepository.findByStatusOrderByReceivedAtAsc(eq(InboxStatus.FAILED), any(PageRequest.class)))
                .thenReturn(List.of(row));
        when(inboxService.requeueFailed(eventId)).thenReturn(1);

        relay.replayFailedEvents();

        verify(inboxService).requeueFailed(eventId);
    }
}
