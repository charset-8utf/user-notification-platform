package com.crud.notification;

/**
 * Тип события пользователя, публикуемого в шину user-notifications.
 * Контракт согласован с потребителем (notification-service).
 */
public enum UserNotificationOperation {
    USER_CREATED,
    USER_DELETED
}
