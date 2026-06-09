package com.crud.config;

import com.crud.notification.compensation.NotificationCompensationEvent;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

@Configuration
@Profile("kafka")
@EnableKafka
public class KafkaCompensationConsumerConfig {

    @Bean
    public ConsumerFactory<String, NotificationCompensationEvent> compensationConsumerFactory(
            KafkaProperties properties, JsonMapper jsonMapper
    ) {
        Map<String, Object> config = properties.buildConsumerProperties();
        JacksonJsonDeserializer<NotificationCompensationEvent> deserializer =
                new JacksonJsonDeserializer<>(NotificationCompensationEvent.class, jsonMapper, false);
        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, NotificationCompensationEvent>
    compensationKafkaListenerContainerFactory(
            ConsumerFactory<String, NotificationCompensationEvent> compensationConsumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, NotificationCompensationEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(compensationConsumerFactory);
        return factory;
    }
}
