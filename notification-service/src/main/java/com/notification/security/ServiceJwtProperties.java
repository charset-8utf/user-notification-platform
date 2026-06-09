package com.notification.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.service-jwt")
public record ServiceJwtProperties(
        String secret,
        String issuer,
        String audience,
        String subject,
        String scope
) {
    public ServiceJwtProperties {
        if (issuer == null || issuer.isBlank()) {
            issuer = "user-notification-platform";
        }
        if (audience == null || audience.isBlank()) {
            audience = "notification-service";
        }
        if (subject == null || subject.isBlank()) {
            subject = "user-service";
        }
        if (scope == null || scope.isBlank()) {
            scope = "notifications:write";
        }
    }
}
