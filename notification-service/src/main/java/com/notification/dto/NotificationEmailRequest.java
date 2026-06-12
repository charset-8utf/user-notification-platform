package com.notification.dto;

import com.notification.domain.UserNotificationOperation;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Контракт REST и Kafka (топик {@code user-notifications}).
 * {@code eventId} — ключ идемпотентности на стороне consumer (at-least-once).
 */
public record NotificationEmailRequest(
        @NotNull(message = "eventId не может быть null")
        UUID eventId,

        @NotNull(message = "Операция не может быть null")
        UserNotificationOperation operation,

        @NotBlank(message = "Email не может быть пустым")
        @Email(message = "Некорректный формат email")
        String email
) {

    public static NotificationEmailRequest of(UserNotificationOperation operation, String email) {
        return new NotificationEmailRequest(UUID.randomUUID(), operation, email);
    }
}
