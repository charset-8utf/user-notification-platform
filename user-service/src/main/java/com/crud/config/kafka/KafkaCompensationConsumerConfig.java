package com.crud.config.kafka;

import com.crud.notification.compensation.NotificationCompensationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;

@Configuration
@Profile("kafka")
@EnableKafka
@RequiredArgsConstructor
public class KafkaCompensationConsumerConfig {

    private final JsonCompensationConsumerFactoryBuilder consumerFactoryBuilder;

    @Bean
    public ConsumerFactory<String, NotificationCompensationEvent> compensationConsumerFactory(
            KafkaProperties properties
    ) {
        return consumerFactoryBuilder.build(properties);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, NotificationCompensationEvent>
    compensationKafkaListenerContainerFactory(
            ConsumerFactory<String, NotificationCompensationEvent> compensationConsumerFactory
    ) {
        return KafkaConsumerConfigurationSupport.simpleListenerFactory(compensationConsumerFactory);
    }
}
