package com.notification.lookup;

import com.notification.service.port.UserCacheView;
import com.notification.service.port.UserLookupPort;
import lombok.RequiredArgsConstructor;
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
 * Adapter (GoF): чтение enrichment из Redis-кэша user-service ({@code user:email:{email}}).
 */
@Component
@Profile("redis")
@Slf4j
@RequiredArgsConstructor
public class RedisUserLookupPort implements UserLookupPort {

    private static final String KEY_PREFIX_EMAIL = "user:email:";

    private final ObjectProvider<StringRedisTemplate> redis;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<UserCacheView> findByEmail(String email) {
        if (email.isBlank()) {
            return Optional.empty();
        }
        String key = KEY_PREFIX_EMAIL + email;
        try {
            String json = redis.getObject().opsForValue().get(key);
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
