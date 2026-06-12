package com.crud.config.rest;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.notification.rest.tls")
public record NotificationRestTlsProperties(
        String trustStore,
        String trustStorePassword,
        String trustStoreType
) {
}
