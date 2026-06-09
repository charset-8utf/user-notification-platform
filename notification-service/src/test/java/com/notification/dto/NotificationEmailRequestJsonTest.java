package com.notification.dto;

import com.notification.entity.UserNotificationOperation;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationEmailRequestJsonTest {

    @Test
    void deserializesConsoleProducerDltPayload() {
        JsonMapper jsonMapper = JsonMapper.builder().build();
        String json = "{\"eventId\":\"550e8400-e29b-41d4-a716-446655440000\","
                + "\"operation\":\"USER_CREATED\",\"email\":\"comp@example.com\"}";

        NotificationEmailRequest request = jsonMapper.readValue(json, NotificationEmailRequest.class);

        assertThat(request.eventId()).isEqualTo(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
        assertThat(request.operation()).isEqualTo(UserNotificationOperation.USER_CREATED);
        assertThat(request.email()).isEqualTo("comp@example.com");
    }
}
