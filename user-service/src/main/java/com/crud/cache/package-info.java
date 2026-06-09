/**
 * Прикладной кэш пользователей в Redis:
 * {@code user:{id}} → {@link com.crud.cache.UserCacheView} (обогащение notification-service),
 * {@code user:query:{id}} → {@link com.crud.dto.UserResponse} (CQRS read-model для GET /api/users/{id}).
 * Реализация выбирается профилем: {@code redis} включает {@link com.crud.cache.RedisUserCachePort},
 * иначе используется {@link com.crud.cache.NoOpUserCachePort} (для тестов и локальной отладки без Redis).
 */
package com.crud.cache;
