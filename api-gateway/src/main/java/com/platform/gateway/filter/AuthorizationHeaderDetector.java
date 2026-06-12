package com.platform.gateway.filter;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Component
public class AuthorizationHeaderDetector {

    public boolean hasAuthorizationHeader(ServerWebExchange exchange) {
        return !isBlank(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
    }

    private static boolean isBlank(@Nullable String value) {
        return value == null || value.isBlank();
    }
}
