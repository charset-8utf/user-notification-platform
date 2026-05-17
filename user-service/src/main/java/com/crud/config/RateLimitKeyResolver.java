package com.crud.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

public final class RateLimitKeyResolver {

    private RateLimitKeyResolver() {
    }

    public static String resolve(HttpServletRequest request) {
        return fromSecurityContext()
                .or(() -> fromAuthorizationHeader(request))
                .orElseGet(() -> "ip:" + remoteAddress(request));
    }

    private static Optional<String> fromSecurityContext() {
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

    private static Optional<String> fromAuthorizationHeader(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isBlank()) {
            return Optional.empty();
        }
        if (authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return subjectFromJwtPayload(authorization.substring(7).trim());
        }
        if (authorization.regionMatches(true, 0, "Basic ", 0, 6)) {
            return basicUsername(authorization.substring(6).trim());
        }
        return Optional.empty();
    }

    private static Optional<String> subjectFromJwtPayload(String token) {
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

    private static Optional<String> basicUsername(String base64Credentials) {
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

    private static String remoteAddress(HttpServletRequest request) {
        return Optional.ofNullable(request.getRemoteAddr()).filter(s -> !s.isBlank()).orElse("unknown");
    }
}
