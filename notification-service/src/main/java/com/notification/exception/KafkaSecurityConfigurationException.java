package com.notification.exception;

/**
 * Ошибка настройки SASL_SSL / trust store для Kafka.
 */
public class KafkaSecurityConfigurationException extends NotificationServiceException {

    public KafkaSecurityConfigurationException(String message) {
        super(message);
    }

    public KafkaSecurityConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
