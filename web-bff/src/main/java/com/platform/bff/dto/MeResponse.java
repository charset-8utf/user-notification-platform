package com.platform.bff.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MeResponse(
        UserSummary user,
        ProfileSummary profile,
        NotificationSummary lastNotification) {
}
