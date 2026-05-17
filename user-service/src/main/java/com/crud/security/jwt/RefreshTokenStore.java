package com.crud.security.jwt;

import java.time.Duration;
import java.util.Optional;

public interface RefreshTokenStore {

    void store(String tokenId, RefreshTokenRecord tokenRecord, Duration ttl);

    Optional<RefreshTokenRecord> find(String tokenId);

    void delete(String tokenId);

    void blacklist(String tokenId, Duration ttl);

    boolean isBlacklisted(String tokenId);
}
