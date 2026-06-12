package com.crud.security.servicejwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.security.service-jwt")
public record ServiceJwtProperties(
        String secret,
        @DefaultValue("user-notification-platform") String issuer,
        @DefaultValue("user-service") String subject,
        @DefaultValue("notification-service") String audience,
        @DefaultValue("notifications:write") String scope,
        @DefaultValue("5m") Duration tokenTtl
) {
}
