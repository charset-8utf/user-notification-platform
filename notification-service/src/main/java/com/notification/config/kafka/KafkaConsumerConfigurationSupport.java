package com.notification.config.kafka;

import com.notification.dto.NotificationEmailRequest;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Stateless-хелперы для JSON и Avro Kafka consumer config: listener factory и DLT error handler.
 */
final class KafkaConsumerConfigurationSupport {

    private KafkaConsumerConfigurationSupport() {
    }

    static ConcurrentKafkaListenerContainerFactory<String, NotificationEmailRequest> withRetryAndManualAck(
            ConsumerFactory<String, NotificationEmailRequest> consumerFactory,
            int concurrency,
            DefaultErrorHandler errorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, NotificationEmailRequest> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(concurrency);
        factory.setCommonErrorHandler(errorHandler);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    static ConcurrentKafkaListenerContainerFactory<String, NotificationEmailRequest> forDltTopic(
            ConsumerFactory<String, NotificationEmailRequest> consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, NotificationEmailRequest> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(1);
        return factory;
    }

    static DefaultErrorHandler deadLetterErrorHandler(
            KafkaOperations<String, Object> kafkaTemplate,
            int maxAttempts,
            long backoffMs,
            String dltSuffix
    ) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (ConsumerRecord<?, ?> consumerRecord, Exception ex) ->
                        new TopicPartition(consumerRecord.topic() + dltSuffix, consumerRecord.partition())
        );
        long retries = Math.max(0, maxAttempts - 1L);
        return new DefaultErrorHandler(recoverer, new FixedBackOff(backoffMs, retries));
    }
}
