package com.crud.config;

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
 * Producer для профиля {@code kafka}: JSON через {@link JacksonJsonSerializer}.
 */
@Configuration
@Profile("kafka")
public class KafkaProducerConfig {

    @Bean
    public JacksonJsonSerializer<Object> kafkaValueSerializer(JsonMapper jsonMapper) {
        return new JacksonJsonSerializer<>(jsonMapper).noTypeInfo();
    }

    @Bean(destroyMethod = "destroy")
    public ProducerFactory<String, Object> producerFactory(
            KafkaProperties properties,
            JacksonJsonSerializer<Object> kafkaValueSerializer) {
        Map<String, Object> config = properties.buildProducerProperties();
        return new DefaultKafkaProducerFactory<>(
                config,
                new StringSerializer(),
                kafkaValueSerializer
        );
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
