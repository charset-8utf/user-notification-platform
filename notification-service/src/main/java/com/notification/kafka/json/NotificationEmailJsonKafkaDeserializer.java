package com.notification.kafka.json;

import com.notification.dto.NotificationEmailRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
@Profile("kafka")
@ConditionalOnProperty(prefix = "app.kafka", name = "serialization", havingValue = "json", matchIfMissing = true)
@RequiredArgsConstructor
public class NotificationEmailJsonKafkaDeserializer {

    private final JsonMapper jsonMapper;

    public JacksonJsonDeserializer<NotificationEmailRequest> create() {
        return new JacksonJsonDeserializer<>(NotificationEmailRequest.class, jsonMapper, false);
    }
}
