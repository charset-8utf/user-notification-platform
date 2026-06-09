package com.crud.cache;

import com.crud.entity.NotificationDeliveryStatus;

import com.crud.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisUserCachePortTest {

    @Mock
    private StringRedisTemplate redis;
    @Mock
    private ValueOperations<String, String> valueOps;

    private ObjectMapper objectMapper;
    private RedisUserCachePort cache;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        lenient().when(redis.opsForValue()).thenReturn(valueOps);
        cache = new RedisUserCachePort(redis, objectMapper, Duration.ofMinutes(5));
    }

    @Test
    void put_writesByIdAndEmailKeys() throws Exception {
        UserCacheView view = UserCacheView.active(1L, "user@example.com");
        String json = objectMapper.writeValueAsString(view);

        cache.put(view);

        verify(valueOps).set("user:1", json, Duration.ofMinutes(5));
        verify(valueOps).set("user:email:user@example.com", json, Duration.ofMinutes(5));
    }

    @Test
    void putResponse_skipsNullId() {
        cache.putResponse(new UserResponse(null, "n", "e@example.com", 20, NotificationDeliveryStatus.PENDING, LocalDateTime.now()));

        verify(valueOps, never()).set(any(), any(), any(Duration.class));
    }

    @Test
    void putResponse_writesQueryKey() throws Exception {
        UserResponse response = new UserResponse(7L, "Ann", "ann@example.com", 30, NotificationDeliveryStatus.PENDING, LocalDateTime.now());
        String json = objectMapper.writeValueAsString(response);

        cache.putResponse(response);

        verify(valueOps).set("user:query:7", json, Duration.ofMinutes(5));
    }

    @Test
    void findResponseById_returnsCachedValue() throws Exception {
        UserResponse response = new UserResponse(3L, "Bob", "bob@example.com", 40, NotificationDeliveryStatus.PENDING, LocalDateTime.of(2026, 5, 31, 12, 0));
        when(valueOps.get("user:query:3")).thenReturn(objectMapper.writeValueAsString(response));

        Optional<UserResponse> found = cache.findResponseById(3L);

        assertThat(found).isPresent();
        assertThat(found.get().id()).isEqualTo(3L);
        assertThat(found.get().email()).isEqualTo("bob@example.com");
    }

    @Test
    void findResponseById_returnsEmptyWhenMissing() {
        when(valueOps.get("user:query:99")).thenReturn(null);

        assertThat(cache.findResponseById(99L)).isEmpty();
    }

    @Test
    void evict_removesIdQueryAndEmailKeys() throws Exception {
        UserCacheView view = UserCacheView.active(5L, "evict@example.com");
        when(valueOps.get("user:5")).thenReturn(objectMapper.writeValueAsString(view));

        cache.evict(5L);

        verify(redis).delete("user:5");
        verify(redis).delete("user:query:5");
        verify(redis).delete("user:email:evict@example.com");
    }

    @Test
    void put_logsWarningOnSerializationError() throws Exception {
        ObjectMapper failingMapper = org.mockito.Mockito.mock(ObjectMapper.class);
        when(failingMapper.writeValueAsString(any())).thenThrow(new JacksonException("bad json") {});
        RedisUserCachePort failingCache = new RedisUserCachePort(redis, failingMapper, Duration.ofMinutes(5));

        failingCache.put(UserCacheView.active(2L, "bad@example.com"));

        verify(valueOps, never()).set(any(), any(), any(Duration.class));
    }

    @Test
    void putResponse_logsWarningOnRedisError() {
        UserResponse response = new UserResponse(8L, "X", "x@example.com", 25, NotificationDeliveryStatus.PENDING, LocalDateTime.now());
        doThrow(new DataAccessException("redis down") {}).when(valueOps).set(eq("user:query:8"), any(), any(Duration.class));

        cache.putResponse(response);
    }

    @Test
    void findResponseById_returnsEmptyOnReadError() {
        when(valueOps.get("user:query:11")).thenThrow(new DataAccessException("redis down") {});

        assertThat(cache.findResponseById(11L)).isEmpty();
    }

    @Test
    void evict_logsWarningOnError() {
        when(valueOps.get("user:12")).thenThrow(new DataAccessException("redis down") {});

        cache.evict(12L);
    }
}
