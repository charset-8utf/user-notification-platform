package com.notification.config.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.notification.kafka")
public record NotificationKafkaProperties(
        @DefaultValue("user-notifications") String topic,
        @DefaultValue("3") int partitions,
        @DefaultValue("1") short replicas,
        @DefaultValue(".DLT") String dltSuffix,
        @DefaultValue("notification-service-dlt") String dltListenerGroup,
        @DefaultValue("notification-compensations") String compensationTopic,
        @DefaultValue Listener listener,
        @DefaultValue Inbox inbox,
        @DefaultValue Retry retry
) {

    public record Listener(@DefaultValue("3") int concurrency) {
    }

    public record Inbox(
            @DefaultValue("50") int batchSize,
            @DefaultValue("1000") long relayIntervalMs,
            @DefaultValue("30000") long failedReplayIntervalMs,
            @DefaultValue("300000") long staleProcessingTimeoutMs
    ) {
    }

    public record Retry(
            @DefaultValue("3") int maxAttempts,
            @DefaultValue("1000") long backoffMs
    ) {
    }
}
