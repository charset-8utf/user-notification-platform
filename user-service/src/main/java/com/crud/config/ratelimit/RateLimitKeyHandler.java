package com.crud.config.ratelimit;

import java.util.Optional;

/**
 * Chain of Responsibility: каждый обработчик пытается построить ключ rate limit.
 */
@FunctionalInterface
public interface RateLimitKeyHandler {

    Optional<String> handle(RateLimitKeyContext context);
}
