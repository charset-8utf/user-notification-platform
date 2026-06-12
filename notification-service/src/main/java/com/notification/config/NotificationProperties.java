package com.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.notification")
public record NotificationProperties(
        @DefaultValue("ваш сайт") String siteName,
        @DefaultValue("noreply@localhost") String mailFrom
) {
}
