package com.crud.security.jwt;

import com.crud.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisRefreshTokenStoreTest {

    @Mock
    private StringRedisTemplate redis;
    @Mock
    private ValueOperations<String, String> valueOps;

    private RedisRefreshTokenStore store;

    @BeforeEach
    void setUp() {
        when(redis.opsForValue()).thenReturn(valueOps);
        ObjectMapper mapper = new ObjectMapper();
        JwtProperties props = new JwtProperties(
                "test-jwt-secret-for-unit-tests-min-32b", "user-service", null, null,
                Duration.ofMinutes(15), Duration.ofDays(7));
        store = new RedisRefreshTokenStore(redis, mapper, props);
    }

    @Test
    void store_writesJsonWithTtl() {
        RefreshTokenRecord tokenRecord = new RefreshTokenRecord("admin", List.of("ADMIN"));
        store.store("id1", tokenRecord, Duration.ofHours(1));
        verify(valueOps).set(eq("auth:refresh:id1"), any(String.class), eq(Duration.ofHours(1)));
    }

    @Test
    void find_returnsRecordWhenPresent() {
        RefreshTokenRecord tokenRecord = new RefreshTokenRecord("admin", List.of("ADMIN"));
        String json = new ObjectMapper().writeValueAsString(tokenRecord);
        when(redis.hasKey("auth:refresh:blacklist:id1")).thenReturn(false);
        when(valueOps.get("auth:refresh:id1")).thenReturn(json);

        assertThat(store.find("id1")).contains(tokenRecord);
    }

    @Test
    void blacklist_setsKeyAndDeletesToken() {
        when(redis.hasKey("auth:refresh:blacklist:id1")).thenReturn(true);
        assertThat(store.isBlacklisted("id1")).isTrue();
        store.blacklist("id2", Duration.ofMinutes(10));
        verify(valueOps).set("auth:refresh:blacklist:id2", "1", Duration.ofMinutes(10));
        verify(redis).delete("auth:refresh:id2");
    }
}
