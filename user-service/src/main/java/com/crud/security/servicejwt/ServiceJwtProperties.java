package com.crud.security.servicejwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.security.service-jwt")
public record ServiceJwtProperties(
        String secret,
        String issuer,
        String subject,
        String audience,
        String scope,
        Duration tokenTtl
) {
    public ServiceJwtProperties {
        if (issuer == null || issuer.isBlank()) {
            issuer = "user-notification-platform";
        }
        if (subject == null || subject.isBlank()) {
            subject = "user-service";
        }
        if (audience == null || audience.isBlank()) {
            audience = "notification-service";
        }
        if (scope == null || scope.isBlank()) {
            scope = "notifications:write";
        }
        if (tokenTtl == null) {
            tokenTtl = Duration.ofMinutes(5);
        }
    }
}
