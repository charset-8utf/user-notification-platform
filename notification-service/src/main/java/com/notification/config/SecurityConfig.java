package com.notification.config;

import com.notification.security.ServiceJwtProperties;
import com.notification.security.ServiceJwtSecurityConfig;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JsonMapper jsonMapper,
            org.springframework.security.oauth2.jwt.JwtDecoder serviceJwtDecoder,
            JwtAuthenticationConverter serviceJwtAuthenticationConverter,
            ServiceJwtProperties serviceJwtProperties
    ) {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/info", "/actuator/prometheus").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/notifications/email")
                        .hasAuthority(ServiceJwtSecurityConfig.scopeAuthority(serviceJwtProperties))
                        .requestMatchers("/api/**").denyAll()
                        .anyRequest().denyAll())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(serviceJwtDecoder)
                                .jwtAuthenticationConverter(serviceJwtAuthenticationConverter)))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            log.warn("Unauthorized API access: {} {}", request.getMethod(), request.getRequestURI());
                            writeJsonError(response, jsonMapper, HttpServletResponse.SC_UNAUTHORIZED,
                                    "Unauthorized", "Valid service JWT required", request.getRequestURI());
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            log.warn("Forbidden API access: {} {}", request.getMethod(), request.getRequestURI());
                            writeJsonError(response, jsonMapper, HttpServletResponse.SC_FORBIDDEN,
                                    "Forbidden", "Access denied", request.getRequestURI());
                        }));

        return http.build();
    }

    private static void writeJsonError(
            HttpServletResponse response,
            JsonMapper jsonMapper,
            int status,
            String error,
            String message,
            String path
    ) {
        try {
            response.setStatus(status);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            Map<String, Object> body = Map.of(
                    "timestamp", Instant.now().toString(),
                    "status", status,
                    "error", error,
                    "message", message,
                    "path", path);
            jsonMapper.writeValue(response.getOutputStream(), body);
        } catch (IOException ex) {
            log.error("Failed to write security error response for path={}", path, ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
