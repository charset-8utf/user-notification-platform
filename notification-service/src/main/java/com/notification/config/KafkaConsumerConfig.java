package com.notification.config;

import com.notification.dto.NotificationEmailRequest;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

/**
 * Consumer-конфиг для профиля {@code kafka}: {@link JacksonJsonDeserializer} (Jackson 3)
 * без опоры на type-headers, целевой тип — {@link NotificationEmailRequest};
 * плюс ограниченное число попыток и DLT для «битых» сообщений.
 */
@Configuration
@Profile("kafka")
@EnableKafka
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, NotificationEmailRequest> consumerFactory(
            KafkaProperties properties, JsonMapper jsonMapper
    ) {
        Map<String, Object> config = properties.buildConsumerProperties();
        JacksonJsonDeserializer<NotificationEmailRequest> jsonDeserializer =
                new JacksonJsonDeserializer<>(NotificationEmailRequest.class, jsonMapper, false);
        ErrorHandlingDeserializer<NotificationEmailRequest> valueDeserializer =
                new ErrorHandlingDeserializer<>(jsonDeserializer);
        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                valueDeserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, NotificationEmailRequest> kafkaListenerContainerFactory(
            ConsumerFactory<String, NotificationEmailRequest> consumerFactory,
            DefaultErrorHandler kafkaErrorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, NotificationEmailRequest> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(kafkaErrorHandler);
        // at-least-once: commit offset только после acknowledge в listener
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    /**
     * После {@code maxAttempts} неуспешных попыток (включая первую) сообщение публикуется
     * в DLT-топик с суффиксом {@code dltSuffix} и тем же партиционированием.
     */
    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            KafkaOperations<String, Object> kafkaTemplate,
            @Value("${app.notification.kafka.retry.max-attempts:3}") int maxAttempts,
            @Value("${app.notification.kafka.retry.backoff-ms:1000}") long backoffMs,
            @Value("${app.notification.kafka.dlt-suffix:.DLT}") String dltSuffix
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
