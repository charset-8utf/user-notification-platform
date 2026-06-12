package com.crud.config.kafka;

import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

/** Stateless-хелперы для Kafka consumer listener factories. */
final class KafkaConsumerConfigurationSupport {

    private KafkaConsumerConfigurationSupport() {
    }

    static <T> ConcurrentKafkaListenerContainerFactory<String, T> simpleListenerFactory(
            ConsumerFactory<String, T> consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, T> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.BATCH);
        return factory;
    }
}
