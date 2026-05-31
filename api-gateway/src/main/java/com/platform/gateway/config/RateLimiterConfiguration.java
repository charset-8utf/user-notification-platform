package com.platform.gateway.config;

import com.platform.gateway.ratelimit.IpGatewayRateLimitKeyStrategy;
import com.platform.gateway.ratelimit.JwtGatewayRateLimitKeyStrategy;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RateLimiterConfiguration {

    @Bean
    @Primary
    KeyResolver ipKeyResolver(IpGatewayRateLimitKeyStrategy ipStrategy) {
        return ipStrategy::resolve;
    }

    @Bean(name = JwtSubKeyResolver.BEAN_NAME)
    KeyResolver jwtSubKeyResolver(JwtGatewayRateLimitKeyStrategy jwtStrategy) {
        return new JwtSubKeyResolver(jwtStrategy);
    }
}
