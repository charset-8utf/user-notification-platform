package com.notification.lookup;

import java.util.Optional;

/**
 * Точка enrichment'а перед отправкой письма: позволяет подтянуть
 * актуальный {@link UserCacheView} (id, status, ...), не блокируя основной поток
 * на HTTP к user-service. По умолчанию активна реализация {@link NoOpUserLookupPort},
 * под профилем {@code redis} — {@link RedisUserLookupPort}.
 */
public interface UserLookupPort {

    Optional<UserCacheView> findByEmail(String email);
}
