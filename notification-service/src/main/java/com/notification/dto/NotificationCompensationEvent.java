package com.notification.dto;

import com.notification.entity.UserNotificationOperation;

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
