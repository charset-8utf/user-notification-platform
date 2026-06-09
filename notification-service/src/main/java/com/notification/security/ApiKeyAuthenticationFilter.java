package com.notification.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * API Key auth (7AuthConcepts): credential интеграции через {@code X-API-Key}.
 * Применяется только к write-endpoint, если Bearer service JWT отсутствует.
 */
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    public static final String API_KEY_HEADER = "X-API-Key";
    public static final String WRITE_PATH = "/api/notifications/email";

    private final ApiKeyProperties properties;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (properties.enabled()
                && "POST".equalsIgnoreCase(request.getMethod())
                && WRITE_PATH.equals(request.getRequestURI())
                && SecurityContextHolder.getContext().getAuthentication() == null
                && hasNoBearer(request)) {
            String apiKey = request.getHeader(API_KEY_HEADER);
            if (apiKey != null && isValid(apiKey)) {
                var auth = new ApiKeyAuthenticationToken(apiKey);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        filterChain.doFilter(request, response);
    }

    private static boolean hasNoBearer(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        return authorization == null || !authorization.regionMatches(true, 0, "Bearer ", 0, 7);
    }

    private boolean isValid(String apiKey) {
        Set<String> allowed = properties.resolvedKeys().stream()
                .filter(k -> k != null && !k.isBlank())
                .collect(Collectors.toSet());
        return allowed.contains(apiKey);
    }

    static final class ApiKeyAuthenticationToken extends AbstractAuthenticationToken {

        private final String apiKey;

        ApiKeyAuthenticationToken(String apiKey) {
            super(AuthorityUtils.createAuthorityList("SCOPE_notifications:write"));
            this.apiKey = apiKey;
            setAuthenticated(true);
        }

        @Override
        public Object getCredentials() {
            return apiKey;
        }

        @Override
        public Object getPrincipal() {
            return "api-key-client";
        }
    }
}
