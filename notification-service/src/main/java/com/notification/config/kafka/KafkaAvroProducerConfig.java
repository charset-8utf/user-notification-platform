package com.notification.config.kafka;

import com.platform.kafka.contracts.KafkaAvroClientProperties;
import com.platform.kafka.contracts.UserNotificationEventAvroSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Profile("kafka")
@ConditionalOnProperty(prefix = "app.kafka", name = "serialization", havingValue = "avro")
public class KafkaAvroProducerConfig {

    @Bean
    public DefaultKafkaProducerFactory<String, Object> avroProducerFactory(
            KafkaProperties properties,
            AppKafkaProperties appKafkaProperties
    ) {
        Map<String, Object> config = new HashMap<>(properties.buildProducerProperties());
        config.putAll(KafkaAvroClientProperties.avroSerdeConfig(appKafkaProperties.schemaRegistryUrl()));
        return new DefaultKafkaProducerFactory<>(
                config,
                new StringSerializer(),
                new UserNotificationEventAvroSerializer()
        );
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> avroProducerFactory) {
        return new KafkaTemplate<>(avroProducerFactory);
    }
}
