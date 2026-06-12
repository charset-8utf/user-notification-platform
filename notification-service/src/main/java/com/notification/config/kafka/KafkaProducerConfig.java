package com.notification.config.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import com.notification.kafka.json.JsonNotificationEmailProducerFactoryBuilder;

@Configuration
@Profile("kafka")
@RequiredArgsConstructor
public class KafkaProducerConfig {

    private final JsonNotificationEmailProducerFactoryBuilder producerFactoryBuilder;

    @Bean(destroyMethod = "destroy")
    public DefaultKafkaProducerFactory<String, Object> jsonProducerFactory(KafkaProperties properties) {
        return producerFactoryBuilder.build(properties);
    }

    @Bean("compensationKafkaTemplate")
    public KafkaTemplate<String, Object> compensationKafkaTemplate(
            ProducerFactory<String, Object> jsonProducerFactory) {
        return new KafkaTemplate<>(jsonProducerFactory);
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.kafka", name = "serialization", havingValue = "json", matchIfMissing = true)
    public KafkaTemplate<String, Object> kafkaTemplate(
            @Qualifier("compensationKafkaTemplate") KafkaTemplate<String, Object> compensationKafkaTemplate) {
        return compensationKafkaTemplate;
    }
}
