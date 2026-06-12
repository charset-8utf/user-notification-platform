package com.notification.domain;

/**
 * Согласовано с контрактом Kafka в user-service (топик {@code user-notifications}).
 */
public enum UserNotificationOperation {
    USER_CREATED,
    USER_DELETED
}
