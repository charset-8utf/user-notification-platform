package com.notification.exception;

/**
 * Ошибка при запросе consumer lag через Kafka Admin API.
 */
public class KafkaLagMonitorException extends NotificationServiceException {

    public KafkaLagMonitorException(String message, Throwable cause) {
        super(message, cause);
    }
}
