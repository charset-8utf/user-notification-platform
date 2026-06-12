package com.crud.config.ratelimit;

final class RateLimitKeyHandlerChains {

    private RateLimitKeyHandlerChains() {
    }

    static RateLimitKeyHandlerChain create() {
        return new RateLimitKeyHandlerChain(new RateLimitKeyHandlerAssembly().head());
    }
}
