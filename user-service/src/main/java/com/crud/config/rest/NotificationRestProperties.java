package com.crud.config.rest;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.notification.rest")
public record NotificationRestProperties(
        @DefaultValue("https://notification-service:8443") String baseUrl,
        @DefaultValue("false") boolean insecureSsl,
        @DefaultValue("PT2S") Duration connectTimeout,
        @DefaultValue("PT5S") Duration readTimeout
) {
}
