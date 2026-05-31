package com.notification.dto;

import java.time.LocalDateTime;

public record NotificationLogSummaryResponse(
        boolean found,
        String operation,
        String channel,
        String status,
        String email,
        LocalDateTime createdAt,
        String detail
) {
}
