package com.crud.config.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.notification.kafka")
public record UserNotificationKafkaProperties(
        @DefaultValue("user-notifications") String topic,
        @DefaultValue("3") int partitions,
        @DefaultValue("1") short replicas,
        @DefaultValue("notification-compensations") String compensationTopic,
        @DefaultValue("user-service-compensation") String compensationConsumerGroup,
        @DefaultValue Outbox outbox
) {

    public record Outbox(
            @DefaultValue("1000") long relayIntervalMs,
            @DefaultValue("50") int batchSize,
            @DefaultValue("30000") long failedReplayIntervalMs
    ) {
    }
}
