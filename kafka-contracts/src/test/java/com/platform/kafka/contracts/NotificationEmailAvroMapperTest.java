package com.platform.kafka.contracts;

import com.platform.kafka.avro.NotificationEmailMessage;
import com.platform.kafka.avro.UserNotificationOperation;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationEmailAvroMapperTest {

    @Test
    void mapsEventFieldsToAvroAndBack() {
        UUID eventId = UUID.randomUUID();
        NotificationEmailMessage message = NotificationEmailAvroMapper.toAvro(
                eventId, UserNotificationOperation.USER_CREATED, "user@example.com");

        assertThat(message.getEventId()).isEqualTo(eventId.toString());
        assertThat(message.getOperation()).isEqualTo(UserNotificationOperation.USER_CREATED);
        assertThat(message.getEmail()).isEqualTo("user@example.com");
        assertThat(NotificationEmailAvroMapper.eventId(message)).isEqualTo(eventId);
        assertThat(NotificationEmailAvroMapper.operationName(message)).isEqualTo("USER_CREATED");
    }
}
