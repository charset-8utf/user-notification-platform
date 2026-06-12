package com.platform.gateway.filter;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Component
public class AuthorizationHeaderExchangeMutator {

    public ServerWebExchange withAuthorization(ServerWebExchange exchange, String authorization) {
        return exchange.mutate()
                .request(exchange.getRequest().mutate()
                        .header(HttpHeaders.AUTHORIZATION, authorization)
                        .build())
                .build();
    }
}
