package com.crud.config.ratelimit;

import java.util.Optional;

@FunctionalInterface
public interface RateLimitKeyHandler {

    Optional<String> handle(RateLimitKeyContext context);
}
