package com.platform.gateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import static org.assertj.core.api.Assertions.assertThat;

class AuthorizationHeaderExchangeMutatorTest {

    private final AuthorizationHeaderExchangeMutator mutator = new AuthorizationHeaderExchangeMutator();

    @Test
    void addsAuthorizationHeaderToExchange() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/users/1").build());

        var enriched = mutator.withAuthorization(exchange, "Bearer jwt-token");

        assertThat(enriched.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .isEqualTo("Bearer jwt-token");
    }
}
