package com.platform.gateway.ratelimit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtGatewayRateLimitKeyStrategy implements GatewayRateLimitKeyStrategy {

    private final JwtSubKeyExtractor jwtSubKeyExtractor;
    private final IpGatewayRateLimitKeyStrategy ipFallbackStrategy;

    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        String authorization = exchange.getRequest().getHeaders().getFirst("Authorization");
        return Mono.justOrEmpty(jwtSubKeyExtractor.subjectKeyFromBearer(authorization))
                .switchIfEmpty(ipFallbackStrategy.resolve(exchange));
    }
}
