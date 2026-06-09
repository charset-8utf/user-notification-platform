package com.notification.config;

import com.notification.security.SecurityJsonErrorWriter;
import com.notification.security.ServiceJwtAuthorities;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final SecurityJsonErrorWriter securityJsonErrorWriter;
    private final ServiceJwtAuthorities serviceJwtAuthorities;

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
                            log.warn("Unauthorized read API: {} {}", request.getMethod(), request.getRequestURI());
                            securityJsonErrorWriter.write(response, HttpServletResponse.SC_UNAUTHORIZED,
                                    "Unauthorized", "Valid user JWT required", request.getRequestURI());
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            log.warn("Forbidden read API: {} {}", request.getMethod(), request.getRequestURI());
                            securityJsonErrorWriter.write(response, HttpServletResponse.SC_FORBIDDEN,
                                    "Forbidden", accessDeniedException.getMessage(), request.getRequestURI());
                        }));
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain serviceNotificationWriteChain(
            HttpSecurity http,
            JwtDecoder serviceJwtDecoder,
            JwtAuthenticationConverter serviceJwtAuthenticationConverter
    ) {
        http.securityMatcher(request -> !request.getRequestURI().startsWith("/api/notifications/logs"))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/info", "/actuator/prometheus").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/notifications/email")
                        .hasAuthority(serviceJwtAuthorities.writeScopeAuthority())
                        .requestMatchers("/api/**").denyAll()
                        .anyRequest().denyAll())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(serviceJwtDecoder)
                                .jwtAuthenticationConverter(serviceJwtAuthenticationConverter)))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            log.warn("Unauthorized API access: {} {}", request.getMethod(), request.getRequestURI());
                            securityJsonErrorWriter.write(response, HttpServletResponse.SC_UNAUTHORIZED,
                                    "Unauthorized", "Valid service JWT required", request.getRequestURI());
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            log.warn("Forbidden API access: {} {}", request.getMethod(), request.getRequestURI());
                            securityJsonErrorWriter.write(response, HttpServletResponse.SC_FORBIDDEN,
                                    "Forbidden", "Access denied", request.getRequestURI());
                        }));

        return http.build();
    }
}
