package com.notification.dto;

import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.Objects;

public record NotificationLogSummaryResponse(
        boolean found,
        @Nullable String operation,
        @Nullable String channel,
        @Nullable String status,
        String email,
        @Nullable LocalDateTime createdAt,
        String detail
) {

    public NotificationLogSummaryResponse {
        Objects.requireNonNull(email, "email");
        Objects.requireNonNull(detail, "detail");
    }
}
