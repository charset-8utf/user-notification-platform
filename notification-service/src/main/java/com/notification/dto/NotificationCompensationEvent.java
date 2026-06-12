package com.notification.dto;

import com.notification.domain.UserNotificationOperation;

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
