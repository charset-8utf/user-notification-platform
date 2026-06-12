package com.notification.mapper;

import com.notification.domain.NotificationChannel;
import com.notification.domain.NotificationDeliveryStatus;
import com.notification.domain.UserNotificationOperation;
import com.notification.dto.NotificationEmailRequest;
import com.notification.entity.NotificationLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationLogMapperTest {

    private NotificationLogMapper mapper;

    @BeforeEach
    void setUp() {
        NotificationLogMapperImpl impl = new NotificationLogMapperImpl();
        ReflectionTestUtils.setField(impl, "notificationLogDetailResolver", new NotificationLogDetailResolver());
        mapper = impl;
    }

    @Test
    void toEntity_mapsRequestStatusAndChannel() {
        NotificationEmailRequest request = new NotificationEmailRequest(
                UUID.fromString("990e8400-e29b-41d4-a716-446655440099"),
                UserNotificationOperation.USER_CREATED,
                "user@example.com");

        NotificationLog entity = mapper.toEntity(request, NotificationDeliveryStatus.SENT, null);

        assertThat(entity.getId()).isNotBlank();
        assertThat(entity.getOperation()).isEqualTo(UserNotificationOperation.USER_CREATED);
        assertThat(entity.getEmail()).isEqualTo("user@example.com");
        assertThat(entity.getChannel()).isEqualTo(NotificationChannel.EMAIL);
        assertThat(entity.getStatus()).isEqualTo(NotificationDeliveryStatus.SENT);
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getErrorMessage()).isNull();
    }

    @Test
    void toSummary_usesDetailResolver() {
        NotificationLog log = NotificationLog.builder()
                .operation(UserNotificationOperation.USER_DELETED)
                .email("user@example.com")
                .channel(NotificationChannel.EMAIL)
                .status(NotificationDeliveryStatus.FAILED)
                .errorMessage("smtp down")
                .build();

        var summary = mapper.toSummary(log);

        assertThat(summary.found()).isTrue();
        assertThat(summary.operation()).isEqualTo("USER_DELETED");
        assertThat(summary.channel()).isEqualTo("EMAIL");
        assertThat(summary.status()).isEqualTo("FAILED");
        assertThat(summary.email()).isEqualTo("user@example.com");
        assertThat(summary.detail()).isEqualTo("smtp down");
    }

    @Test
    void toSummary_defaultsDetailToOkWhenNoError() {
        NotificationLog log = NotificationLog.builder()
                .operation(UserNotificationOperation.USER_CREATED)
                .email("user@example.com")
                .channel(NotificationChannel.EMAIL)
                .status(NotificationDeliveryStatus.SENT)
                .build();

        assertThat(mapper.toSummary(log).detail()).isEqualTo("OK");
    }
}
