package com.crud.config.security;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(
        String secret,
        @DefaultValue("user-service") String issuer,
        @Nullable String issuerUri,
        @Nullable String jwkSetUri,
        @DefaultValue("PT15M") Duration accessTokenTtl,
        @DefaultValue("P7D") Duration refreshTokenTtl
) {
    public boolean oidcEnabled() {
        return issuerUri != null && !issuerUri.isBlank();
    }

    public String requiredIssuerUri() {
        if (issuerUri == null || issuerUri.isBlank()) {
            throw new IllegalStateException("При OIDC требуется app.security.jwt.issuer-uri");
        }
        return issuerUri;
    }

    public JwtProperties {
        if (issuer.isBlank()) {
            issuer = "user-service";
        }
    }
}
