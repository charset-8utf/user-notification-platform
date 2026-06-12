package com.crud.config.ratelimit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitPropertiesTest {

    @Test
    void defaultsMatchApplicationYaml() {
        RateLimitProperties properties = new RateLimitProperties(20, 60);

        assertThat(properties.maxRequests()).isEqualTo(20);
        assertThat(properties.windowSeconds()).isEqualTo(60);
    }
}
