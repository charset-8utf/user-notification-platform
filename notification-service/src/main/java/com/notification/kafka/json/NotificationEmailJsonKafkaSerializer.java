package com.notification.kafka.json;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
@Profile("kafka")
@RequiredArgsConstructor
public class NotificationEmailJsonKafkaSerializer {

    private final JsonMapper jsonMapper;

    /**
     * Создаёт сериализатор для Kafka producer.
     * <p>
     * {@link JacksonJsonSerializer} реализует {@link java.lang.AutoCloseable}, но здесь не используется
     * try-with-resources: жизненный цикл сериализатора управляется {@link org.springframework.kafka.core.DefaultKafkaProducerFactory}
     * через Supplier — factory создаёт и закрывает сериализатор вместе с producer'ом.
     */
    @SuppressWarnings({"resource", "java:S2095"})
    public JacksonJsonSerializer<Object> create() {
        return new JacksonJsonSerializer<>(jsonMapper).noTypeInfo();
    }
}
