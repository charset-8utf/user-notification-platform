package com.platform.gateway.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ReactiveSecurityContextTokenRelayExchangeResolver implements TokenRelayExchangeResolver {

    private final AuthorizationHeaderExchangeMutator authorizationHeaderExchangeMutator;

    @Override
    public Mono<ServerWebExchange> resolve(ServerWebExchange exchange) {
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(context -> jwtExchange(exchange, context))
                .defaultIfEmpty(exchange);
    }

    private Mono<ServerWebExchange> jwtExchange(ServerWebExchange exchange, SecurityContext context) {
        if (!(context.getAuthentication() instanceof JwtAuthenticationToken jwtAuth)) {
            return Mono.empty();
        }
        String bearer = "Bearer " + jwtAuth.getToken().getTokenValue();
        return Mono.just(authorizationHeaderExchangeMutator.withAuthorization(exchange, bearer));
    }
}
