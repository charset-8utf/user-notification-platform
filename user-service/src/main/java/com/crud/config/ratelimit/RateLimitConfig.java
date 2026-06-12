package com.crud.config.ratelimit;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class RateLimitConfig {

    static final String RATE_LIMIT_KEY_HANDLER_HEAD = "rateLimitKeyHandlerHead";

    @Bean(name = RATE_LIMIT_KEY_HANDLER_HEAD)
    RateLimitKeyHandler rateLimitKeyHandlerHead(RateLimitKeyHandlerAssembly assembly) {
        return assembly.head();
    }

    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> rateLimitFilter(
            JsonMapper jsonMapper,
            RateLimitKeyResolver rateLimitKeyResolver,
            RateLimitProperties rateLimitProperties) {
        FilterRegistrationBean<OncePerRequestFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RateLimitFilter(
                jsonMapper,
                rateLimitKeyResolver,
                rateLimitProperties.maxRequests(),
                rateLimitProperties.windowSeconds()));
        registration.addUrlPatterns("/api/*");
        registration.setOrder(-99);
        return registration;
    }
}
