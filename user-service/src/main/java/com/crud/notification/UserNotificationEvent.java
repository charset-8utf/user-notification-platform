package com.crud.notification;

import java.util.UUID;

public record UserNotificationEvent(UUID eventId, UserNotificationOperation operation, String email) {

    public static UserNotificationEvent create(UserNotificationOperation operation, String email) {
        return new UserNotificationEvent(UUID.randomUUID(), operation, email);
    }
}
