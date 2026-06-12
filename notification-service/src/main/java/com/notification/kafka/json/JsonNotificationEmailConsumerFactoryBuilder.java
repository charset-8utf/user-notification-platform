package com.notification.kafka.json;

import com.notification.dto.NotificationEmailRequest;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.stereotype.Component;

@Component
@Profile("kafka")
@ConditionalOnProperty(prefix = "app.kafka", name = "serialization", havingValue = "json", matchIfMissing = true)
@RequiredArgsConstructor
public class JsonNotificationEmailConsumerFactoryBuilder {

    private final NotificationEmailJsonKafkaDeserializer notificationEmailJsonKafkaDeserializer;

    public ConsumerFactory<String, NotificationEmailRequest> build(KafkaProperties properties) {
        return new DefaultKafkaConsumerFactory<>(
                properties.buildConsumerProperties(),
                new StringDeserializer(),
                new ErrorHandlingDeserializer<>(notificationEmailJsonKafkaDeserializer.create())
        );
    }

    public ConsumerFactory<String, NotificationEmailRequest> buildForDlt(KafkaProperties properties) {
        return new DefaultKafkaConsumerFactory<>(
                properties.buildConsumerProperties(),
                new StringDeserializer(),
                notificationEmailJsonKafkaDeserializer.create()
        );
    }
}
