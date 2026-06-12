package com.crud.config.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

/**
 * Producer для профиля {@code kafka}: JSON через {@link org.springframework.kafka.support.serializer.JacksonJsonSerializer}.
 */
@Configuration
@Profile("kafka")
@ConditionalOnProperty(prefix = "app.kafka", name = "serialization", havingValue = "json", matchIfMissing = true)
@RequiredArgsConstructor
public class KafkaProducerConfig {

    private final JsonUserNotificationProducerFactoryBuilder producerFactoryBuilder;

    @Bean(destroyMethod = "destroy")
    public DefaultKafkaProducerFactory<String, Object> producerFactory(KafkaProperties properties) {
        return producerFactoryBuilder.build(properties);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
