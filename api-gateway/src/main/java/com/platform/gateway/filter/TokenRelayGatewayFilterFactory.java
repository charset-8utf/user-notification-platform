package com.platform.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Relays the validated user JWT to downstream services.
 * <p>
 * Spring Cloud's built-in {@code TokenRelay} requires OAuth2 Client ({@code oauth2Login}).
 * This gateway validates bearer JWT at the edge (resource server), so we relay either the
 * incoming {@code Authorization} header or the token from the reactive security context.
 */
@Component
public class TokenRelayGatewayFilterFactory
        extends AbstractGatewayFilterFactory<AbstractGatewayFilterFactory.NameConfig> {

    public TokenRelayGatewayFilterFactory() {
        super(NameConfig.class);
    }

    @Override
    public GatewayFilter apply(NameConfig config) {
        return (exchange, chain) -> {
            String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authorization != null && !authorization.isBlank()) {
                return chain.filter(exchange);
            }
            return ReactiveSecurityContextHolder.getContext()
                    .map(securityContext -> securityContext.getAuthentication())
                    .filter(JwtAuthenticationToken.class::isInstance)
                    .cast(JwtAuthenticationToken.class)
                    .map(JwtAuthenticationToken::getToken)
                    .map(jwt -> withAuthorization(exchange, "Bearer " + jwt.getTokenValue()))
                    .flatMap(chain::filter)
                    .switchIfEmpty(chain.filter(exchange));
        };
    }

    private static ServerWebExchange withAuthorization(ServerWebExchange exchange, String authorization) {
        return exchange.mutate()
                .request(exchange.getRequest().mutate()
                        .header(HttpHeaders.AUTHORIZATION, authorization)
                        .build())
                .build();
    }
}
