package com.platform.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.gateway.ssl")
public record GatewaySslProperties(
        @DefaultValue("true") boolean insecureTrustManager
) {
}
