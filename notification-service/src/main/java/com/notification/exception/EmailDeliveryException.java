package com.notification.exception;

public class EmailDeliveryException extends NotificationServiceException {

    public EmailDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
