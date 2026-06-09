package com.crud.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka.security")
public record KafkaSecurityProperties(
        boolean enabled,
        String username,
        String password,
        String trustStore,
        String trustStorePassword,
        String trustStoreType,
        String endpointIdentificationAlgorithm
) {
}
