package com.notification.config;

import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

/**
 * Producer-конфиг используется в основном тестами (notification-service сам по себе — консьюмер).
 * Значения — {@link JacksonJsonSerializer} на Jackson 3 ({@code tools.jackson}), без type-headers в Kafka.
 */
@Configuration
@Profile("kafka")
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, Object> producerFactory(KafkaProperties properties, JsonMapper jsonMapper) {
        Map<String, Object> config = properties.buildProducerProperties();
        return new DefaultKafkaProducerFactory<>(
                config,
                new StringSerializer(),
                new JacksonJsonSerializer<>(jsonMapper).noTypeInfo()
        );
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
