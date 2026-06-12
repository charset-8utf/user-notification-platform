package com.notification.service.port;

/**
 * Read-model пользователя из Redis-кэша user-service.
 */
public record UserCacheView(Long id, String email, String status) {
}
