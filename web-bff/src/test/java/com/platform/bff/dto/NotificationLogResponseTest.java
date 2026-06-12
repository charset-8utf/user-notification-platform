package com.platform.bff.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationLogResponseTest {

    @Test
    void toSummary_whenNotFound_returnsNoneStatus() {
        NotificationSummary summary = new NotificationLogResponse(
                false, null, null, null, "user@example.com", null, null)
                .toSummary();

        assertThat(summary.channel()).isEqualTo("EMAIL");
        assertThat(summary.status()).isEqualTo("NONE");
        assertThat(summary.detail()).isEqualTo("Уведомлений пока нет");
    }

    @Test
    void toSummary_whenFound_mapsDeliveryFields() {
        NotificationSummary summary = new NotificationLogResponse(
                true, "USER_CREATED", "email", "DELIVERED", "user@example.com", null, "Welcome")
                .toSummary();

        assertThat(summary.channel()).isEqualTo("email");
        assertThat(summary.status()).isEqualTo("DELIVERED");
        assertThat(summary.detail()).isEqualTo("Welcome");
    }
}
