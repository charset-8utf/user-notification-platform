package com.platform.kafka.contracts;

import com.platform.kafka.avro.NotificationEmailMessage;
import com.platform.kafka.avro.UserNotificationOperation;

import java.util.UUID;

public final class NotificationEmailAvroMapper {

    private NotificationEmailAvroMapper() {
    }

    public static NotificationEmailMessage toAvro(UUID eventId, String operation, String email) {
        return NotificationEmailMessage.newBuilder()
                .setEventId(eventId.toString())
                .setOperation(UserNotificationOperation.valueOf(operation))
                .setEmail(email)
                .build();
    }

    public static NotificationEmailMessage toAvro(UUID eventId, Enum<?> operation, String email) {
        return toAvro(eventId, operation.name(), email);
    }

    public static UUID eventId(NotificationEmailMessage message) {
        return UUID.fromString(message.getEventId());
    }

    public static String operationName(NotificationEmailMessage message) {
        return message.getOperation().name();
    }
}
