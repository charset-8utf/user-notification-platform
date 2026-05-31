package com.crud.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class RateLimitConfig {

    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> rateLimitFilter(
            JsonMapper jsonMapper,
            RateLimitKeyResolver rateLimitKeyResolver,
            @Value("${app.rate-limit.max-requests:20}") int maxRequests,
            @Value("${app.rate-limit.window-seconds:60}") long windowSeconds) {
        FilterRegistrationBean<OncePerRequestFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RateLimitFilter(jsonMapper, rateLimitKeyResolver, maxRequests, windowSeconds));
        registration.addUrlPatterns("/api/*");
        registration.setOrder(-99);
        return registration;
    }
}
