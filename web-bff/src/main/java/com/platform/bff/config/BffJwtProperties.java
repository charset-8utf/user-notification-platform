package com.platform.bff.config;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.jwt")
public record BffJwtProperties(@Nullable String secret) {

    public String requireSecret() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("app.security.jwt.secret is required");
        }
        return secret;
    }
}
