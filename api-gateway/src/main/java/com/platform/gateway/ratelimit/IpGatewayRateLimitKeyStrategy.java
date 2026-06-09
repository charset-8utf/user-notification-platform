package com.platform.gateway.ratelimit;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

@Component
public class IpGatewayRateLimitKeyStrategy implements GatewayRateLimitKeyStrategy {

    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        return Mono.justOrEmpty(exchange.getRequest().getRemoteAddress())
                .map(InetSocketAddress::getAddress)
                .map(address -> "ip:" + address.getHostAddress())
                .defaultIfEmpty("ip:unknown");
    }
}
