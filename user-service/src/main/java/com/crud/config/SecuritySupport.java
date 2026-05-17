package com.crud.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

@Slf4j
final class SecuritySupport {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_USER = "USER";
    private static final int UNAUTHORIZED_STATUS = HttpServletResponse.SC_UNAUTHORIZED;
    private static final String UNAUTHORIZED_ERROR = "Unauthorized";
    private static final String UNAUTHORIZED_MESSAGE = "Authentication required";

    private SecuritySupport() {
    }

    static void configureAuthorization(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth
    ) {
        auth.requestMatchers("/actuator/health", "/actuator/info", "/actuator/prometheus").permitAll()
                .requestMatchers("/api/auth/login", "/api/auth/refresh").permitAll()
                .requestMatchers("/api/users/**").hasAnyRole(ROLE_USER, ROLE_ADMIN)
                .requestMatchers("/api/profiles/**").hasAnyRole(ROLE_USER, ROLE_ADMIN)
                .requestMatchers("/api/roles/assign", "/api/roles/remove").hasRole(ROLE_ADMIN)
                .requestMatchers("/api/roles/**").hasAnyRole(ROLE_USER, ROLE_ADMIN)
                .anyRequest().authenticated();
    }

    static void configureApiSecurityHeaders(HttpSecurity http) {
        http.headers(headers -> headers
                .contentTypeOptions(Customizer.withDefaults())
                .frameOptions(frame -> frame.deny())
                .contentSecurityPolicy(csp -> csp
                        .policyDirectives("default-src 'none'; frame-ancestors 'none'")));
    }

    static void configureJsonUnauthorized(HttpSecurity http, JsonMapper jsonMapper) {
        http.exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
            log.warn("Unauthorized access attempt: {} {}", request.getMethod(), request.getRequestURI());
            writeUnauthorizedJson(response, jsonMapper, request.getRequestURI());
        }));
    }

    private static void writeUnauthorizedJson(
            HttpServletResponse response,
            JsonMapper jsonMapper,
            String path
    ) {
        try {
            response.setStatus(UNAUTHORIZED_STATUS);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            Map<String, Object> body = Map.of(
                    "timestamp", Instant.now().toString(),
                    "status", UNAUTHORIZED_STATUS,
                    "error", UNAUTHORIZED_ERROR,
                    "message", UNAUTHORIZED_MESSAGE,
                    "path", path);
            jsonMapper.writeValue(response.getOutputStream(), body);
        } catch (IOException ex) {
            log.error("Failed to write security error response for path={}", path, ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
