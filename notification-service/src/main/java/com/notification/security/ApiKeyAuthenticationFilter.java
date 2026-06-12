package com.notification.security;

import com.notification.config.NotificationApiProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * API Key auth: credential интеграции через настраиваемый заголовок (по умолчанию {@code X-API-Key}).
 * Применяется только к write-endpoint, если Bearer service JWT отсутствует.
 */
@Component
@ConditionalOnProperty(prefix = "app.security.api-key", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final ApiKeyProperties properties;
    private final NotificationApiProperties notificationApiProperties;
    private final ServiceJwtAuthorities serviceJwtAuthorities;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if ("POST".equalsIgnoreCase(request.getMethod())
                && notificationApiProperties.resolvedEmailPath().equals(request.getRequestURI())
                && SecurityContextHolder.getContext().getAuthentication() == null
                && hasNoBearer(request)) {
            String apiKey = request.getHeader(properties.getHeader());
            if (apiKey != null && isValid(apiKey)) {
                var auth = new ApiKeyAuthenticationToken(apiKey, serviceJwtAuthorities.writeScopeAuthority());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean hasNoBearer(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        return authorization == null || !authorization.regionMatches(true, 0, "Bearer ", 0, 7);
    }

    private boolean isValid(String apiKey) {
        return properties.resolvedKeys().contains(apiKey);
    }
}
