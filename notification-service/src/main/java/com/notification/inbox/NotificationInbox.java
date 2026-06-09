package com.notification.inbox;

import com.notification.entity.UserNotificationOperation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Transactional inbox: событие из Kafka сохраняется до обработки;
 * {@link KafkaInboxRelay} доставляет письмо и переводит статус.
 */
@Document("notification_inbox")
@CompoundIndex(name = "idx_inbox_status_received", def = "{'status': 1, 'receivedAt': 1}")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationInbox {

    @Id
    private String eventId;

    private UserNotificationOperation operation;

    private String email;

    private InboxStatus status;

    private LocalDateTime receivedAt;

    private LocalDateTime processedAt;

    public static NotificationInbox pending(UUID eventId, UserNotificationOperation operation, String email) {
        return NotificationInbox.builder()
                .eventId(eventId.toString())
                .operation(operation)
                .email(email)
                .status(InboxStatus.PENDING)
                .receivedAt(LocalDateTime.now())
                .build();
    }
}
