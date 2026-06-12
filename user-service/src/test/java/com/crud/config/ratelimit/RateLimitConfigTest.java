package com.crud.config.ratelimit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitConfigTest {

    @Test
    void registersRateLimitFilterForApiPaths() {
        RateLimitConfig config = new RateLimitConfig();
        FilterRegistrationBean<OncePerRequestFilter> registration = config.rateLimitFilter(
                JsonMapper.builder().build(),
                new RateLimitKeyResolver(RateLimitKeyHandlerChains.create()),
                new RateLimitProperties(15, 30));

        assertThat(registration.getFilter()).isInstanceOf(RateLimitFilter.class);
        assertThat(registration.getUrlPatterns()).containsExactly("/api/*");
        assertThat(registration.getOrder()).isEqualTo(-99);
    }
}
