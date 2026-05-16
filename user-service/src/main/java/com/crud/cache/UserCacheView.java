package com.crud.cache;

/**
 * Минимальная проекция пользователя для Redis-кэша.
 * Поле {@code status} зарезервировано контрактом плана и пока всегда {@code ACTIVE}.
 */
public record UserCacheView(Long id, String email, String status) {

    public static UserCacheView active(Long id, String email) {
        return new UserCacheView(id, email, "ACTIVE");
    }
}
