package com.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.notification.api")
public record NotificationApiProperties(
        @DefaultValue("") String emailPath
) {

    public String resolvedEmailPath() {
        if (emailPath.isBlank()) {
            throw new IllegalStateException(
                    "Задайте app.notification.api.email-path (или APP_NOTIFICATION_EMAIL_PATH)");
        }
        return emailPath;
    }
}
