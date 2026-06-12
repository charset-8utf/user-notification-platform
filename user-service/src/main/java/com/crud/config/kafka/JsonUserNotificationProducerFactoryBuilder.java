package com.crud.config.kafka;

import com.crud.notification.kafka.UserNotificationJsonKafkaSerializer;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Profile("kafka")
@RequiredArgsConstructor
public class JsonUserNotificationProducerFactoryBuilder {

    private final UserNotificationJsonKafkaSerializer userNotificationJsonKafkaSerializer;

    public DefaultKafkaProducerFactory<String, Object> build(KafkaProperties properties) {
        Map<String, Object> config = properties.buildProducerProperties();
        return new DefaultKafkaProducerFactory<>(
                config,
                StringSerializer::new,
                userNotificationJsonKafkaSerializer::create
        );
    }
}
