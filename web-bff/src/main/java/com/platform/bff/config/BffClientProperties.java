package com.platform.bff.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.bff")
public record BffClientProperties(
        @DefaultValue("https://user-service") String userServiceBaseUrl,
        @DefaultValue("https://notification-service") String notificationServiceBaseUrl,
        @DefaultValue("true") boolean loadBalanced,
        @DefaultValue("false") boolean insecureSsl
) {
}
