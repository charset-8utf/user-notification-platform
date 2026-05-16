package com.crud.cache;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Redis-кэш пользователей: ключ {@code user:{id}}, значение — JSON {@link UserCacheView}.
 * TTL настраивается через {@code app.cache.redis.ttl} (ISO-8601 duration).
 */
@Component
@Profile("redis")
@Slf4j
public class RedisUserCachePort implements UserCachePort {

    private static final String KEY_PREFIX_ID = "user:";
    private static final String KEY_PREFIX_EMAIL = "user:email:";

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final Duration ttl;

    public RedisUserCachePort(
            StringRedisTemplate redis,
            ObjectMapper objectMapper,
            @Value("${app.cache.redis.ttl:PT1H}") Duration ttl
    ) {
        this.redis = redis;
        this.objectMapper = objectMapper;
        this.ttl = ttl;
    }

    @Override
    public void put(UserCacheView view) {
        String byId = KEY_PREFIX_ID + view.id();
        String byEmail = KEY_PREFIX_EMAIL + view.email();
        try {
            String json = objectMapper.writeValueAsString(view);
            redis.opsForValue().set(byId, json, ttl);
            redis.opsForValue().set(byEmail, json, ttl);
            log.debug("Redis put: {} / {} ttl={}", byId, byEmail, ttl);
        } catch (JacksonException | DataAccessException e) {
            log.warn("Не удалось записать пользователя в Redis (id={}, email={}): {}",
                    view.id(), view.email(), e.getMessage());
        }
    }

    @Override
    public void evict(Long id) {
        String byId = KEY_PREFIX_ID + id;
        try {
            String existing = redis.opsForValue().get(byId);
            redis.delete(byId);
            if (existing != null) {
                UserCacheView view = objectMapper.readValue(existing, UserCacheView.class);
                redis.delete(KEY_PREFIX_EMAIL + view.email());
            }
            log.debug("Redis evict: id={}", id);
        } catch (JacksonException | DataAccessException e) {
            log.warn("Не удалось удалить пользователя из Redis (id={}): {}", id, e.getMessage());
        }
    }
}
