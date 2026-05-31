package com.crud.config.ratelimit;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

final class BasicAuthRateLimitKeyHandler extends AbstractRateLimitKeyHandler {

    BasicAuthRateLimitKeyHandler(RateLimitKeyHandler next) {
        super(next);
    }

    @Override
    protected Optional<String> doHandle(RateLimitKeyContext context) {
        String authorization = context.request().getHeader("Authorization");
        if (authorization == null || !authorization.regionMatches(true, 0, "Basic ", 0, 6)) {
            return Optional.empty();
        }
        return basicUsername(authorization.substring(6).trim());
    }

    private Optional<String> basicUsername(String base64Credentials) {
        try {
            String decoded = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
            int colon = decoded.indexOf(':');
            if (colon <= 0) {
                return Optional.empty();
            }
            return Optional.of("user:" + decoded.substring(0, colon));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
