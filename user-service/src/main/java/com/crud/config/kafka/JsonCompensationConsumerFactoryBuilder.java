package com.crud.config.kafka;

import com.crud.notification.compensation.NotificationCompensationEvent;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Compensation topic всегда JSON (см. notification-service {@code compensationKafkaTemplate}),
 * независимо от {@code app.kafka.serialization} для user-notifications.
 */
@Component
@Profile("kafka")
@RequiredArgsConstructor
public class JsonCompensationConsumerFactoryBuilder {

    private final JsonMapper jsonMapper;

    public ConsumerFactory<String, NotificationCompensationEvent> build(KafkaProperties properties) {
        JacksonJsonDeserializer<NotificationCompensationEvent> deserializer =
                new JacksonJsonDeserializer<>(NotificationCompensationEvent.class, jsonMapper, false);
        return new DefaultKafkaConsumerFactory<>(
                properties.buildConsumerProperties(),
                new StringDeserializer(),
                deserializer
        );
    }
}
