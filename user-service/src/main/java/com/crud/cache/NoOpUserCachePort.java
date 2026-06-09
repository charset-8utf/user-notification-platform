package com.crud.cache;

import com.crud.dto.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Profile("!redis")
@Slf4j
public class NoOpUserCachePort implements UserCachePort {

    @Override
    public void put(UserCacheView view) {
        log.debug("Redis отключён, put пропущен: {}", view);
    }

    @Override
    public void putResponse(UserResponse response) {
        log.debug("Redis отключён, putResponse пропущен: id={}", response.id());
    }

    @Override
    public Optional<UserResponse> findResponseById(Long id) {
        return Optional.empty();
    }

    @Override
    public void evict(Long id) {
        log.debug("Redis отключён, evict пропущен: id={}", id);
    }
}
