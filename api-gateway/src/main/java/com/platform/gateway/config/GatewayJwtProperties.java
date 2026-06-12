package com.platform.gateway.config;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.security.jwt")
public record GatewayJwtProperties(
        @Nullable String secret,
        @DefaultValue("user-service") String issuer,
        @Nullable String issuerUri,
        @Nullable String jwkSetUri
) {

    public boolean oidcEnabled() {
        return issuerUri != null && !issuerUri.isBlank();
    }

    public boolean hasJwkSetUri() {
        return jwkSetUri != null && !jwkSetUri.isBlank();
    }

    public String requireIssuerUri() {
        if (issuerUri == null || issuerUri.isBlank()) {
            throw new IllegalStateException(
                    "app.security.jwt.issuer-uri обязателен в режиме OIDC");
        }
        return issuerUri;
    }

    public String requireJwkSetUri() {
        if (jwkSetUri == null || jwkSetUri.isBlank()) {
            throw new IllegalStateException(
                    "app.security.jwt.jwk-set-uri должен быть настроен");
        }
        return jwkSetUri;
    }

    public String requireSecret() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "app.security.jwt.secret обязателен, если issuer-uri не задан");
        }
        return secret;
    }
}
