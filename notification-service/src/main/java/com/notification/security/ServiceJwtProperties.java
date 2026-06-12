package com.notification.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.security.service-jwt")
public record ServiceJwtProperties(
        String secret,
        @DefaultValue("user-notification-platform") String issuer,
        @DefaultValue("notification-service") String audience,
        @DefaultValue("user-service") String subject,
        @DefaultValue("notifications:write") String scope
) {
}
