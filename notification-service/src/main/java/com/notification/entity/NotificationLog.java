package com.notification.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/** Документ истории отправленного уведомления. */
@Document("notification_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationLog {

    @Id
    private String id;

    private UserNotificationOperation operation;

    private String email;

    private NotificationChannel channel;

    private NotificationDeliveryStatus status;

    private String errorMessage;

    private LocalDateTime createdAt;
}
