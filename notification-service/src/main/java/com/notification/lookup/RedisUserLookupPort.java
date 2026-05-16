package com.notification.lookup;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

/**
 * Чтение из Redis-кэша по email-индексу {@code user:email:{email}},
 * который пишет user-service.
 */
@Component
@Profile("redis")
@Slf4j
public class RedisUserLookupPort implements UserLookupPort {

    private static final String KEY_PREFIX_EMAIL = "user:email:";

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public RedisUserLookupPort(ObjectProvider<StringRedisTemplate> redis, ObjectMapper objectMapper) {
        this.redis = redis.getObject();
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<UserCacheView> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        String key = KEY_PREFIX_EMAIL + email;
        try {
            String json = redis.opsForValue().get(key);
            if (json == null) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, UserCacheView.class));
        } catch (JacksonException | DataAccessException e) {
            log.warn("Redis lookup упал для key={}: {}", key, e.getMessage());
            return Optional.empty();
        }
    }
}
