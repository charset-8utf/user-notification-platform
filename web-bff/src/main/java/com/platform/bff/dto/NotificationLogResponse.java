package com.platform.bff.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NotificationLogResponse(
        boolean found,
        String operation,
        String channel,
        String status,
        String email,
        LocalDateTime createdAt,
        String detail
) {
    public NotificationSummary toSummary() {
        if (!found) {
            return new NotificationSummary(
                    channel != null ? channel : "EMAIL",
                    "NONE",
                    detail != null ? detail : "Уведомлений пока нет");
        }
        return new NotificationSummary(
                channel != null ? channel : "EMAIL",
                status != null ? status : "UNKNOWN",
                detail != null ? detail : operation);
    }
}
