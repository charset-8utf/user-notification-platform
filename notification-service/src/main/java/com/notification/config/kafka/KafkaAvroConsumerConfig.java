package com.notification.config.kafka;

import com.notification.dto.NotificationEmailRequest;
import com.notification.kafka.avro.AvroNotificationEmailConsumerFactoryBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DefaultErrorHandler;

@Configuration
@Profile("kafka")
@EnableKafka
@ConditionalOnProperty(prefix = "app.kafka", name = "serialization", havingValue = "avro")
@RequiredArgsConstructor
public class KafkaAvroConsumerConfig {

    private final AvroNotificationEmailConsumerFactoryBuilder consumerFactoryBuilder;
    private final NotificationKafkaProperties notificationKafkaProperties;
    private final AppKafkaProperties appKafkaProperties;

    @Bean
    public ConsumerFactory<String, NotificationEmailRequest> consumerFactory(KafkaProperties properties) {
        return consumerFactoryBuilder.build(properties, appKafkaProperties.schemaRegistryUrl());
    }

    @Bean
    public ConsumerFactory<String, NotificationEmailRequest> dltConsumerFactory(
            ConsumerFactory<String, NotificationEmailRequest> consumerFactory
    ) {
        return consumerFactory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, NotificationEmailRequest> kafkaListenerContainerFactory(
            ConsumerFactory<String, NotificationEmailRequest> consumerFactory,
            DefaultErrorHandler kafkaErrorHandler
    ) {
        return KafkaConsumerConfigurationSupport.withRetryAndManualAck(
                consumerFactory,
                notificationKafkaProperties.listener().concurrency(),
                kafkaErrorHandler
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, NotificationEmailRequest> dltKafkaListenerContainerFactory(
            ConsumerFactory<String, NotificationEmailRequest> dltConsumerFactory
    ) {
        return KafkaConsumerConfigurationSupport.forDltTopic(dltConsumerFactory);
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaOperations<String, Object> kafkaTemplate) {
        NotificationKafkaProperties.Retry retry = notificationKafkaProperties.retry();
        return KafkaConsumerConfigurationSupport.deadLetterErrorHandler(
                kafkaTemplate,
                retry.maxAttempts(),
                retry.backoffMs(),
                notificationKafkaProperties.dltSuffix());
    }
}
