package com.crud.security.jwt;

import com.crud.config.security.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Optional;

@Component
@Profile("jwt & redis")
@RequiredArgsConstructor
public class RedisRefreshTokenStore implements RefreshTokenStore {

    private static final String REFRESH_PREFIX = "auth:refresh:";
    private static final String BLACKLIST_PREFIX = "auth:refresh:blacklist:";

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final JwtProperties jwtProperties;

    @Override
    public void store(String tokenId, RefreshTokenRecord tokenRecord, Duration ttl) {
        try {
            redis.opsForValue().set(key(tokenId), objectMapper.writeValueAsString(tokenRecord), ttl);
        } catch (JacksonException ex) {
            throw new IllegalStateException("Не удалось сохранить refresh token", ex);
        }
    }

    @Override
    public Optional<RefreshTokenRecord> find(String tokenId) {
        if (isBlacklisted(tokenId)) {
            return Optional.empty();
        }
        String json = redis.opsForValue().get(key(tokenId));
        if (json == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, RefreshTokenRecord.class));
        } catch (JacksonException ex) {
            return Optional.empty();
        }
    }

    @Override
    public void delete(String tokenId) {
        redis.delete(key(tokenId));
    }

    @Override
    public void blacklist(String tokenId, Duration ttl) {
        Duration effective = !ttl.isNegative() && !ttl.isZero()
                ? ttl
                : jwtProperties.refreshTokenTtl();
        redis.opsForValue().set(BLACKLIST_PREFIX + tokenId, "1", effective);
        delete(tokenId);
    }

    @Override
    public boolean isBlacklisted(String tokenId) {
        return Boolean.TRUE.equals(redis.hasKey(BLACKLIST_PREFIX + tokenId));
    }

    private static String key(String tokenId) {
        return REFRESH_PREFIX + tokenId;
    }
}
