package com.notification.lookup;

/**
 * Минимальное представление пользователя из общего кэша user-service.
 * Поля совпадают с {@code com.crud.cache.UserCacheView}, чтобы один и тот же
 * JSON корректно десериализовывался в обоих сервисах.
 * Поле {@code email} нужно для совпадения схемы с user-service.
 */
public record UserCacheView(Long id,
                            String email,
                            String status) {
}
