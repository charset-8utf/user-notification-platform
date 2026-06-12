package com.notification.service.port;

import java.util.Optional;

/**
 * Порт enrichment перед отправкой: подтягивает {@link UserCacheView} из Redis
 * без HTTP к user-service. Реализации — в {@code com.notification.lookup}.
 */
public interface UserLookupPort {

    Optional<UserCacheView> findByEmail(String email);
}
