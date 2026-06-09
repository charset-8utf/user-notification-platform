package com.platform.gateway.config;

import com.platform.gateway.ratelimit.IpGatewayRateLimitKeyStrategy;
import com.platform.gateway.ratelimit.JwtGatewayRateLimitKeyStrategy;
import com.platform.gateway.ratelimit.JwtSubKeyExtractor;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import static org.assertj.core.api.Assertions.assertThat;

class JwtSubKeyResolverTest {

    private final JwtSubKeyResolver resolver = new JwtSubKeyResolver(
            new JwtGatewayRateLimitKeyStrategy(new JwtSubKeyExtractor(), new IpGatewayRateLimitKeyStrategy()));

    @Test
    void resolve_shouldUseSubFromBearerPayload() {
        String token = com.platform.gateway.support.GatewayJwtTestSupport.accessToken();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/users/1")
                        .header("Authorization", "Bearer " + token)
                        .build());

        String key = resolver.resolve(exchange).block();
        assertThat(key).isEqualTo("sub:admin");
    }

    @Test
    void resolve_shouldFallbackToIpWhenNoBearer() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/auth/login")
                        .remoteAddress(new java.net.InetSocketAddress("203.0.113.5", 8080))
                        .build());

        String key = resolver.resolve(exchange).block();
        assertThat(key).isEqualTo("ip:203.0.113.5");
    }
}
