package com.notification.config.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.kafka")
public record AppKafkaProperties(
        @DefaultValue("json") String serialization,
        @DefaultValue("") String schemaRegistryUrl
) {
}
