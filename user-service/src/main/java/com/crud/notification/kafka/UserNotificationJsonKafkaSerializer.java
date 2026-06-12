package com.crud.notification.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
@Profile("kafka")
@RequiredArgsConstructor
public class UserNotificationJsonKafkaSerializer {

    private final JsonMapper jsonMapper;

    /**
     * Создаёт сериализатор для Kafka producer.
     * <p>
     * {@link JacksonJsonSerializer} реализует {@link java.lang.AutoCloseable}; try-with-resources здесь не нужен:
     * {@link org.springframework.kafka.core.DefaultKafkaProducerFactory} создаёт и закрывает сериализатор
     * вместе с producer через {@code valueSerializerSupplier}.
     */
    @SuppressWarnings({"resource", "java:S2095"})
    public JacksonJsonSerializer<Object> create() {
        return new JacksonJsonSerializer<>(jsonMapper).noTypeInfo();
    }
}
