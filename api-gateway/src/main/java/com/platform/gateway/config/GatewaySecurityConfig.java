package com.platform.gateway.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebFluxSecurity
@Profile("cloud & !cloud-it")
@EnableConfigurationProperties(GatewayJwtProperties.class)
public class GatewaySecurityConfig {

    @Bean
    SecurityWebFilterChain gatewaySecurityFilterChain(ServerHttpSecurity http, ReactiveJwtDecoder jwtDecoder) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/health", "/actuator/info", "/actuator/prometheus").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/refresh").permitAll()
                        .pathMatchers("/api/users/**", "/api/profiles/**", "/api/roles/**").authenticated()
                        .pathMatchers(HttpMethod.GET, "/api/notifications/logs/**").authenticated()
                        .pathMatchers("/api/notifications/**").denyAll()
                        .anyExchange().denyAll())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtDecoder(jwtDecoder)))
                .build();
    }

    @Bean
    ReactiveJwtDecoder reactiveJwtDecoder(GatewayJwtProperties properties) {
        byte[] secretBytes = properties.secret().getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException("app.security.jwt.secret must be at least 32 bytes for HS256");
        }
        return NimbusReactiveJwtDecoder.withSecretKey(new SecretKeySpec(secretBytes, "HmacSHA256"))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
}
