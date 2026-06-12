package com.platform.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

@Component
public class TokenRelayGatewayFilterFactory
        extends AbstractGatewayFilterFactory<AbstractGatewayFilterFactory.NameConfig> {

    private final TokenRelayExchangeResolver tokenRelayExchangeResolver;
    private final AuthorizationHeaderDetector authorizationHeaderDetector;

    public TokenRelayGatewayFilterFactory(
            TokenRelayExchangeResolver tokenRelayExchangeResolver,
            AuthorizationHeaderDetector authorizationHeaderDetector) {
        super(NameConfig.class);
        this.tokenRelayExchangeResolver = tokenRelayExchangeResolver;
        this.authorizationHeaderDetector = authorizationHeaderDetector;
    }

    @Override
    public GatewayFilter apply(NameConfig config) {
        return (exchange, chain) -> {
            if (authorizationHeaderDetector.hasAuthorizationHeader(exchange)) {
                return chain.filter(exchange);
            }
            return tokenRelayExchangeResolver.resolve(exchange)
                    .flatMap(chain::filter);
        };
    }
}
