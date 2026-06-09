package com.notification.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.security.jwt")
public record UserJwtProperties(
        String secret,
        @DefaultValue("user-service") String issuer
) {
}
