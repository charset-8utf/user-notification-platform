package com.platform.gateway.config;

import com.platform.gateway.ratelimit.GatewayRateLimitKeyStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class JwtSubKeyResolver implements KeyResolver {

    public static final String BEAN_NAME = "jwtSubKeyResolver";

    private final GatewayRateLimitKeyStrategy jwtStrategy;

    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        return jwtStrategy.resolve(exchange);
    }
}
