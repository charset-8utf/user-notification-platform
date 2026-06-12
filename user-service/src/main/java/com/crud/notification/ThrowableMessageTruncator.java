package com.crud.notification;

import org.springframework.stereotype.Component;

@Component
public class ThrowableMessageTruncator {

    public static final int DEFAULT_MAX_LENGTH = 2000;

    public String truncate(Throwable cause) {
        return truncate(cause, DEFAULT_MAX_LENGTH);
    }

    public String truncate(Throwable cause, int maxLength) {
        String message = cause.getMessage();
        if (message == null) {
            return cause.getClass().getSimpleName();
        }
        return message.length() <= maxLength ? message : message.substring(0, maxLength);
    }
}
