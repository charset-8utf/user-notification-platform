package com.crud.notification.compensation;

import com.crud.notification.UserNotificationOperation;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationCompensationEventJsonTest {

    @Test
    void deserializesConsoleProducerCompensationPayload() throws Exception {
        JsonMapper jsonMapper = JsonMapper.builder().build();
        String json = """
                {"originalEventId":"550e8400-e29b-41d4-a716-446655440000","originalOperation":"USER_CREATED","email":"comp@example.com","errorMessage":"e2e compensation","failedAt":"2026-06-09T19:15:20Z"}
                """;

        NotificationCompensationEvent event = jsonMapper.readValue(json, NotificationCompensationEvent.class);

        assertThat(event.email()).isEqualTo("comp@example.com");
        assertThat(event.originalOperation()).isEqualTo(UserNotificationOperation.USER_CREATED);
        assertThat(event.originalEventId()).isEqualTo(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
    }
}
