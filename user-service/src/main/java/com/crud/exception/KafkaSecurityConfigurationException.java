package com.crud.exception;

/**
 * Ошибка настройки SASL_SSL / trust store для Kafka.
 */
public class KafkaSecurityConfigurationException extends UserServiceException {

    public KafkaSecurityConfigurationException(String message) {
        super(message);
    }

    public KafkaSecurityConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
