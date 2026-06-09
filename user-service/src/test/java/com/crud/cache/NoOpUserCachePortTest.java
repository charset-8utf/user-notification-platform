package com.crud.cache;

import com.crud.entity.NotificationDeliveryStatus;

import com.crud.dto.UserResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class NoOpUserCachePortTest {

    private final NoOpUserCachePort cache = new NoOpUserCachePort();

    @Test
    void methodsAreNoOps() {
        cache.put(UserCacheView.active(1L, "noop@example.com"));
        cache.putResponse(new UserResponse(1L, "Noop", "noop@example.com", 20, NotificationDeliveryStatus.PENDING, LocalDateTime.now()));
        cache.evict(1L);

        assertThat(cache.findResponseById(1L)).isEmpty();
    }
}
