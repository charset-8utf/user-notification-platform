package com.notification.config.security;

import com.notification.config.NotificationApiProperties;
import com.notification.security.ApiKeyAuthenticationFilter;
import com.notification.security.SecurityJsonErrorWriter;
import com.notification.security.ServiceJwtAuthorities;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Две цепочки Spring Security: read API (user JWT) и write API (service JWT / API key).
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private static final String ACCESS_DENIED = "Доступ запрещён";

    private final SecurityJsonErrorWriter securityJsonErrorWriter;
    private final ServiceJwtAuthorities serviceJwtAuthorities;
    private final NotificationApiProperties notificationApiProperties;
    private final ObjectProvider<ApiKeyAuthenticationFilter> apiKeyAuthenticationFilter;

    /** GET /api/notifications/logs/** — Bearer user JWT, роли USER/ADMIN. */
    @Bean
    @Order(1)
    SecurityFilterChain userNotificationReadChain(
            HttpSecurity http,
            JwtDecoder userJwtDecoder,
            JwtAuthenticationConverter userJwtAuthenticationConverter
    ) {
        http.securityMatcher("/api/notifications/logs/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/notifications/logs/**")
                        .hasAnyRole("USER", "ADMIN")
                        .anyRequest().denyAll())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(userJwtDecoder)
                                .jwtAuthenticationConverter(userJwtAuthenticationConverter)))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            log.warn("Неавторизованный доступ к read API: {} {}", request.getMethod(), request.getRequestURI());
                            securityJsonErrorWriter.write(response, HttpServletResponse.SC_UNAUTHORIZED,
                                    "Не авторизован", "Требуется валидный user JWT", request.getRequestURI());
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            log.warn("Запрещённый доступ к read API: {} {}", request.getMethod(), request.getRequestURI());
                            String deniedMessage = accessDeniedException.getMessage();
                            securityJsonErrorWriter.write(response, HttpServletResponse.SC_FORBIDDEN,
                                    ACCESS_DENIED,
                                    deniedMessage != null ? deniedMessage : ACCESS_DENIED,
                                    request.getRequestURI());
                        }));
        return http.build();
    }

    /** POST email и actuator — service JWT (и опционально API key), не logs API. */
    @Bean
    @Order(2)
    SecurityFilterChain serviceNotificationWriteChain(
            HttpSecurity http,
            JwtDecoder serviceJwtDecoder,
            JwtAuthenticationConverter serviceJwtAuthenticationConverter
    ) {
        http.securityMatcher(request -> !request.getRequestURI().startsWith("/api/notifications/logs"))
                .csrf(AbstractHttpConfigurer::disable);
        apiKeyAuthenticationFilter.ifAvailable(filter ->
                http.addFilterBefore(filter, BearerTokenAuthenticationFilter.class));
        http
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/info", "/actuator/prometheus").permitAll()
                        .requestMatchers(HttpMethod.POST, notificationApiProperties.resolvedEmailPath())
                        .hasAuthority(serviceJwtAuthorities.writeScopeAuthority())
                        .requestMatchers("/api/**").denyAll()
                        .anyRequest().denyAll())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(serviceJwtDecoder)
                                .jwtAuthenticationConverter(serviceJwtAuthenticationConverter)))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            log.warn("Неавторизованный доступ к API: {} {}", request.getMethod(), request.getRequestURI());
                            securityJsonErrorWriter.write(response, HttpServletResponse.SC_UNAUTHORIZED,
                                    "Не авторизован", "Требуется валидный service JWT", request.getRequestURI());
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            log.warn("Запрещённый доступ к API: {} {}", request.getMethod(), request.getRequestURI());
                            securityJsonErrorWriter.write(response, HttpServletResponse.SC_FORBIDDEN,
                                    ACCESS_DENIED, ACCESS_DENIED, request.getRequestURI());
                        }));

        return http.build();
    }
}
