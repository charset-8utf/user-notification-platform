package com.crud.config.ratelimit;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

final class BearerJwtRateLimitKeyHandler extends AbstractRateLimitKeyHandler {

    BearerJwtRateLimitKeyHandler(RateLimitKeyHandler next) {
        super(next);
    }

    @Override
    protected Optional<String> doHandle(RateLimitKeyContext context) {
        String authorization = context.request().getHeader("Authorization");
        if (authorization == null || !authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return Optional.empty();
        }
        return subjectFromJwtPayload(authorization.substring(7).trim());
    }

    private Optional<String> subjectFromJwtPayload(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return Optional.empty();
            }
            byte[] json = Base64.getUrlDecoder().decode(parts[1]);
            String payload = new String(json, StandardCharsets.UTF_8);
            String marker = "\"sub\":\"";
            int start = payload.indexOf(marker);
            if (start < 0) {
                return Optional.empty();
            }
            start += marker.length();
            int end = payload.indexOf('"', start);
            if (end < 0) {
                return Optional.empty();
            }
            return Optional.of("sub:" + payload.substring(start, end));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
