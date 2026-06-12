package com.platform.kafka.contracts;

import com.platform.kafka.avro.NotificationEmailMessage;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;
import java.util.UUID;

public class UserNotificationEventAvroSerializer implements Serializer<Object> {

    private final KafkaAvroSerializer delegate = new KafkaAvroSerializer();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        delegate.configure(configs, isKey);
    }

    @Override
    @SuppressWarnings("java:S1168")
    public byte[] serialize(String topic, Object data) {
        return delegate.serialize(topic, data == null ? null : toMessage(data));
    }

    @Override
    public void close() {
        delegate.close();
    }

    private static NotificationEmailMessage toMessage(Object data) {
        if (data instanceof NotificationEmailMessage message) {
            return message;
        }
        if (data instanceof EventPayload(UUID id, Enum<?> operation1, String email1)) {
            return NotificationEmailAvroMapper.toAvro(id, operation1, email1);
        }
        try {
            var eventIdMethod = data.getClass().getMethod("eventId");
            var operationMethod = data.getClass().getMethod("operation");
            var emailMethod = data.getClass().getMethod("email");
            UUID eventId = (UUID) eventIdMethod.invoke(data);
            Enum<?> operation = (Enum<?>) operationMethod.invoke(data);
            String email = (String) emailMethod.invoke(data);
            return NotificationEmailAvroMapper.toAvro(eventId, operation, email);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalArgumentException(
                    "Неподдерживаемый тип Kafka payload для Avro: " + data.getClass().getName(), ex);
        }
    }

    public record EventPayload(UUID eventId, Enum<?> operation, String email) {
    }
}
