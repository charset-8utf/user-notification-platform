/**
 * Прикладной кэш пользователей в Redis: ключ {@code user:{id}} → {@link com.crud.cache.UserCacheView}.
 * Реализация выбирается профилем: {@code redis} включает {@link com.crud.cache.RedisUserCachePort},
 * иначе используется {@link com.crud.cache.NoOpUserCachePort} (для тестов и локальной отладки без Redis).
 */
package com.crud.cache;
