package com.crud.notification.compensation;

import com.crud.notification.UserNotificationOperation;

import java.time.Instant;
import java.util.UUID;

public record NotificationCompensationEvent(
        UUID originalEventId,
        UserNotificationOperation originalOperation,
        String email,
        String errorMessage,
        Instant failedAt
) {
}
