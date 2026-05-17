package com.crud.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!redis")
@Slf4j
public class NoOpUserCachePort implements UserCachePort {

    @Override
    public void put(UserCacheView view) {
        log.debug("Redis отключён, put пропущен: {}", view);
    }

    @Override
    public void evict(Long id) {
        log.debug("Redis отключён, evict пропущен: id={}", id);
    }
}
