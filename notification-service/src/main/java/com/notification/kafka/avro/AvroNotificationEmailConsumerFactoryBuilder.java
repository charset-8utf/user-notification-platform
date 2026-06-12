package com.notification.kafka.avro;

import com.notification.dto.NotificationEmailRequest;
import com.platform.kafka.contracts.KafkaAvroClientProperties;
import com.platform.kafka.contracts.NotificationEmailAvroDeserializer;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Profile("kafka")
@ConditionalOnProperty(prefix = "app.kafka", name = "serialization", havingValue = "avro")
@RequiredArgsConstructor
public class AvroNotificationEmailConsumerFactoryBuilder {

    private final NotificationEmailAvroMessageMapper messageMapper;

    public ConsumerFactory<String, NotificationEmailRequest> build(
            KafkaProperties properties,
            String schemaRegistryUrl
    ) {
        Map<String, Object> config = new HashMap<>(properties.buildConsumerProperties());
        config.putAll(KafkaAvroClientProperties.avroSerdeConfig(schemaRegistryUrl));
        NotificationEmailAvroDeserializer<NotificationEmailRequest> deserializer =
                new NotificationEmailAvroDeserializer<>(messageMapper::toRequest);
        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                deserializer
        );
    }
}
