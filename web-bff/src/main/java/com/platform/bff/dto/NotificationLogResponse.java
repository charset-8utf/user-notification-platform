package com.platform.bff.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NotificationLogResponse(
        boolean found,
        @Nullable String operation,
        @Nullable String channel,
        @Nullable String status,
        @Nullable String email,
        @Nullable LocalDateTime createdAt,
        @Nullable String detail
) {

    public NotificationSummary toSummary() {
        if (!found) {
            return new NotificationSummary(
                    defaultChannel(),
                    "NONE",
                    detail != null ? detail : "Уведомлений пока нет");
        }
        return new NotificationSummary(
                defaultChannel(),
                status != null ? status : "UNKNOWN",
                resolveDetail());
    }

    private String defaultChannel() {
        return channel != null ? channel : "EMAIL";
    }

    private String resolveDetail() {
        if (detail != null) {
            return detail;
        }
        return operation != null ? operation : "UNKNOWN";
    }
}
