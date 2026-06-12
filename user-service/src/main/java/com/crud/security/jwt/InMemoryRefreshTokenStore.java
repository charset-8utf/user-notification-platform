package com.crud.security.jwt;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Profile({"jwt & !redis"})
public class InMemoryRefreshTokenStore implements RefreshTokenStore {

    private final Map<String, RefreshTokenRecord> tokens = new ConcurrentHashMap<>();
    private final Map<String, Instant> blacklist = new ConcurrentHashMap<>();

    @Override
    public void store(String tokenId, RefreshTokenRecord tokenRecord, Duration ttl) {
        tokens.put(tokenId, tokenRecord);
    }

    @Override
    public Optional<RefreshTokenRecord> find(String tokenId) {
        if (isBlacklisted(tokenId)) {
            return Optional.empty();
        }
        return Optional.ofNullable(tokens.get(tokenId));
    }

    @Override
    public void delete(String tokenId) {
        tokens.remove(tokenId);
    }

    @Override
    public void blacklist(String tokenId, Duration ttl) {
        blacklist.put(tokenId, Instant.now().plus(ttl));
        tokens.remove(tokenId);
    }

    @Override
    public boolean isBlacklisted(String tokenId) {
        Instant until = blacklist.get(tokenId);
        if (until == null) {
            return false;
        }
        if (Instant.now().isAfter(until)) {
            blacklist.remove(tokenId);
            return false;
        }
        return true;
    }
}
