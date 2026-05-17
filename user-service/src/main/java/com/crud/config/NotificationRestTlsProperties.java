package com.crud.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.notification.rest.tls")
public record NotificationRestTlsProperties(
        String trustStore,
        String trustStorePassword,
        String trustStoreType
) {
}
