package com.platform.gateway.ratelimit;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public interface GatewayRateLimitKeyStrategy {

    Mono<String> resolve(ServerWebExchange exchange);
}
