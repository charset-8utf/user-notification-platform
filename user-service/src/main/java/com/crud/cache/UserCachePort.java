package com.crud.cache;

/**
 * Абстракция прикладного кэша пользователей.
 * Реализация по умолчанию — no-op; продакшен-вариант — Redis (профиль {@code redis}).
 * Любые ошибки инфраструктуры реализация обязана глотать (логировать), чтобы
 * проблемы кэша не валили бизнес-операции.
 */
public interface UserCachePort {

    void put(UserCacheView view);

    void evict(Long id);
}
