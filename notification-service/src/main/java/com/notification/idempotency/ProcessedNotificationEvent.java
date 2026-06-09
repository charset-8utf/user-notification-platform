package com.notification.idempotency;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

/** Уже обработанные {@code eventId} (идемпотентный consumer, at-least-once). */
@Document("processed_notification_events")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedNotificationEvent {

    @Id
    private String eventId;

    @Indexed
    private LocalDateTime processedAt;

    public ProcessedNotificationEvent(UUID eventId) {
        this.eventId = eventId.toString();
        this.processedAt = LocalDateTime.now();
    }
}
