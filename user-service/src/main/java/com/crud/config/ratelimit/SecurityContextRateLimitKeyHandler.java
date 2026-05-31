package com.crud.config.ratelimit;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

final class SecurityContextRateLimitKeyHandler extends AbstractRateLimitKeyHandler {

    SecurityContextRateLimitKeyHandler(RateLimitKeyHandler next) {
        super(next);
    }

    @Override
    protected Optional<String> doHandle(RateLimitKeyContext context) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            return Optional.of("sub:" + jwt.getSubject());
        }
        String name = authentication.getName();
        if (name != null && !name.isBlank() && !"anonymousUser".equals(name)) {
            return Optional.of("user:" + name);
        }
        return Optional.empty();
    }
}
