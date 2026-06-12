package com.notification.kafka.avro;

import com.notification.domain.UserNotificationOperation;
import com.notification.dto.NotificationEmailRequest;
import com.platform.kafka.avro.NotificationEmailMessage;
import com.platform.kafka.contracts.NotificationEmailAvroMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile("kafka")
@ConditionalOnProperty(prefix = "app.kafka", name = "serialization", havingValue = "avro")
public class NotificationEmailAvroMessageMapper {

    public NotificationEmailRequest toRequest(NotificationEmailMessage message) {
        return new NotificationEmailRequest(
                UUID.fromString(message.getEventId()),
                UserNotificationOperation.valueOf(NotificationEmailAvroMapper.operationName(message)),
                message.getEmail()
        );
    }
}
