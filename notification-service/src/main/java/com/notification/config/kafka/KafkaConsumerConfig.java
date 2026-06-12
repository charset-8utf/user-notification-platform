package com.notification.config.kafka;

import com.notification.dto.NotificationEmailRequest;
import com.notification.kafka.json.JsonNotificationEmailConsumerFactoryBuilder;
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
@ConditionalOnProperty(prefix = "app.kafka", name = "serialization", havingValue = "json", matchIfMissing = true)
@RequiredArgsConstructor
public class KafkaConsumerConfig {

    private final JsonNotificationEmailConsumerFactoryBuilder consumerFactoryBuilder;
    private final NotificationKafkaProperties notificationKafkaProperties;

    @Bean
    public ConsumerFactory<String, NotificationEmailRequest> consumerFactory(KafkaProperties properties) {
        return consumerFactoryBuilder.build(properties);
    }

    @Bean
    public ConsumerFactory<String, NotificationEmailRequest> dltConsumerFactory(KafkaProperties properties) {
        return consumerFactoryBuilder.buildForDlt(properties);
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
