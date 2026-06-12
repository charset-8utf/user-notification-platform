package com.notification.kafka;

import com.notification.config.kafka.NotificationKafkaProperties;
import com.notification.domain.InboxStatus;
import com.notification.domain.UserNotificationOperation;
import com.notification.dto.NotificationEmailRequest;
import com.notification.entity.NotificationInbox;
import com.notification.metrics.InboxMetrics;
import com.notification.service.NotificationInboxService;
import com.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaInboxRelayTest {

    private static final NotificationKafkaProperties KAFKA_PROPERTIES = new NotificationKafkaProperties(
            "user-notifications",
            3,
            (short) 1,
            ".DLT",
            "notification-service-dlt",
            "notification-compensations",
            new NotificationKafkaProperties.Listener(3),
            new NotificationKafkaProperties.Inbox(50, 1000, 30000, 300000),
            new NotificationKafkaProperties.Retry(3, 1000)
    );

    @Mock
    private NotificationInboxService inboxService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private InboxMetrics inboxMetrics;

    @Mock
    private NotificationCompensationPublisher compensationPublisher;

    private KafkaInboxRelay relay;

    @BeforeEach
    void setUp() {
        relay = new KafkaInboxRelay(
                inboxService,
                notificationService,
                inboxMetrics,
                compensationPublisher,
                KAFKA_PROPERTIES);
    }

    @Test
    void relayPendingProcessesEvent() {
        UUID eventId = UUID.randomUUID();
        NotificationInbox row = NotificationInbox.pending(
                eventId, UserNotificationOperation.USER_CREATED, "ok@example.com");
        when(inboxService.claimPendingBatch(50)).thenReturn(List.of(row));

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
        when(inboxService.claimPendingBatch(50)).thenReturn(List.of(row));
        doThrow(new RuntimeException("smtp down")).when(notificationService).sendEmailNotification(any());

        relay.relayPendingEvents();

        NotificationEmailRequest expected = new NotificationEmailRequest(
                eventId, UserNotificationOperation.USER_DELETED, "fail@example.com");
        verify(inboxService).markFailed(eventId);
        verify(inboxMetrics).recordFailed();
        verify(compensationPublisher).publishFromFailedDelivery(expected, "smtp down");
    }

    @Test
    void replayFailedRequeuesRows() {
        UUID eventId = UUID.randomUUID();
        LocalDateTime receivedAt = LocalDateTime.now();
        NotificationInbox row = NotificationInbox.builder()
                .eventId(eventId.toString())
                .operation(UserNotificationOperation.USER_CREATED)
                .email("retry@example.com")
                .status(InboxStatus.FAILED)
                .receivedAt(receivedAt)
                .processedAt(receivedAt)
                .build();
        when(inboxService.findFailedBatch(50)).thenReturn(List.of(row));
        when(inboxService.requeueFailed(eventId)).thenReturn(1);

        relay.replayFailedEvents();

        verify(inboxService).requeueFailed(eventId);
    }
}
