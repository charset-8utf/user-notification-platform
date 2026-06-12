package com.notification.service;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

@Component
public class ErrorMessageTruncator {

    public static final int DEFAULT_MAX_LENGTH = 2000;

    public String truncate(@Nullable String message) {
        return truncate(message, DEFAULT_MAX_LENGTH);
    }

    public String truncate(@Nullable String message, int maxLength) {
        if (message == null) {
            return "unknown";
        }
        return message.length() <= maxLength ? message : message.substring(0, maxLength);
    }
}
