package com.crud.notification;

import java.util.UUID;

/**
 * Событие изменения пользователя.
 * JSON-форма соответствует {@code com.notification.dto.NotificationEmailRequest}
 * на стороне notification-service: {@code {eventId, operation, email}}.
 */
public record UserNotificationEvent(UUID eventId, UserNotificationOperation operation, String email) {

    public static UserNotificationEvent create(UserNotificationOperation operation, String email) {
        return new UserNotificationEvent(UUID.randomUUID(), operation, email);
    }
}
