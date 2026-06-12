package com.notification.lookup;

import com.notification.service.port.UserCacheView;
import com.notification.service.port.UserLookupPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Null Object (GoF): enrichment отключён, когда профиль {@code redis} не включён.
 */
@Component
@Profile("!redis")
public class NoOpUserLookupPort implements UserLookupPort {

    @Override
    public Optional<UserCacheView> findByEmail(String email) {
        return Optional.empty();
    }
}
