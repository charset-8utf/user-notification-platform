package com.platform.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.jwt")
public record GatewayJwtProperties(
        String secret,
        String issuer,
        String issuerUri
) {
    public boolean oidcEnabled() {
        return issuerUri != null && !issuerUri.isBlank();
    }
}
