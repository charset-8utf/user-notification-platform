package com.crud.config.ratelimit;

import org.jspecify.annotations.Nullable;

import java.util.Optional;

abstract class AbstractRateLimitKeyHandler implements RateLimitKeyHandler {

    private final @Nullable RateLimitKeyHandler next;

    protected AbstractRateLimitKeyHandler(@Nullable RateLimitKeyHandler next) {
        this.next = next;
    }

    @Override
    public final Optional<String> handle(RateLimitKeyContext context) {
        Optional<String> key = doHandle(context);
        if (key.isPresent()) {
            return key;
        }
        return next != null ? next.handle(context) : Optional.empty();
    }

    protected abstract Optional<String> doHandle(RateLimitKeyContext context);
}
