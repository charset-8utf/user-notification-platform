package com.platform.gateway.ratelimit;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

@Component
public class JwtSubKeyExtractor {

    public Optional<String> subjectKeyFromBearer(@Nullable String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return Optional.empty();
        }
        return subjectKeyFromPayload(authorizationHeader.substring(7).trim());
    }

    public Optional<String> subjectKeyFromPayload(String token) {
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
