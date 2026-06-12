package com.platform.gateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import static org.assertj.core.api.Assertions.assertThat;

class AuthorizationHeaderDetectorTest {

    private final AuthorizationHeaderDetector detector = new AuthorizationHeaderDetector();

    @Test
    void detectsPresentAuthorizationHeader() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/users/1")
                        .header("Authorization", "Bearer token")
                        .build());

        assertThat(detector.hasAuthorizationHeader(exchange)).isTrue();
    }

    @Test
    void detectsMissingAuthorizationHeader() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/users/1").build());

        assertThat(detector.hasAuthorizationHeader(exchange)).isFalse();
    }
}
