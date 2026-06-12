package com.platform.gateway.filter;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public interface TokenRelayExchangeResolver {

    Mono<ServerWebExchange> resolve(ServerWebExchange exchange);
}
