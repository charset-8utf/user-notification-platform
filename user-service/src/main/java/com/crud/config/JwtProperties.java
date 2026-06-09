package com.crud.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(
        String secret,
        @DefaultValue("user-service") String issuer,
        @DefaultValue("PT15M") Duration accessTokenTtl,
        @DefaultValue("P7D") Duration refreshTokenTtl
) {
    public JwtProperties {
        if (issuer.isBlank()) {
            issuer = "user-service";
        }
    }
}
