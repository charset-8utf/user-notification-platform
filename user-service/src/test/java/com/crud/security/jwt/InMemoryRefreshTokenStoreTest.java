package com.crud.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryRefreshTokenStoreTest {

    private InMemoryRefreshTokenStore store;

    @BeforeEach
    void setUp() {
        store = new InMemoryRefreshTokenStore();
    }

    @Test
    void storeFindDelete_roundTrip() {
        RefreshTokenRecord tokenRecord = new RefreshTokenRecord("admin", List.of("ADMIN"));
        store.store("t1", tokenRecord, Duration.ofMinutes(5));

        assertThat(store.find("t1")).contains(tokenRecord);

        store.delete("t1");
        assertThat(store.find("t1")).isEmpty();
    }

    @Test
    void blacklist_blocksFind() {
        store.store("t1", new RefreshTokenRecord("u", List.of("USER")), Duration.ofMinutes(5));
        store.blacklist("t1", Duration.ofMinutes(1));
        assertThat(store.isBlacklisted("t1")).isTrue();
        assertThat(store.find("t1")).isEmpty();
    }
}
