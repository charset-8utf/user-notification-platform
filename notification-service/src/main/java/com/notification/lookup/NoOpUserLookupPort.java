package com.notification.lookup;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Заглушка, активная когда профиль {@code redis} не включён —
 * notification-service просто работает по данным из самого события.
 */
@Component
@Profile("!redis")
public class NoOpUserLookupPort implements UserLookupPort {

    @Override
    public Optional<UserCacheView> findByEmail(String email) {
        return Optional.empty();
    }
}
