package com.crud.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@Profile("!jwt")
@RequiredArgsConstructor
public class SecurityConfig {

    private final ApiAuthorizationRules apiAuthorizationRules;
    private final ApiHttpSecurityCustomizer apiHttpSecurityCustomizer;

    @Bean
    public SecurityFilterChain basicSecurityFilterChain(
            HttpSecurity http,
            CorsConfigurationSource corsConfigurationSource
    ) {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(apiAuthorizationRules::configure)
                .httpBasic(Customizer.withDefaults());

        apiHttpSecurityCustomizer.configureSecurityHeaders(http);
        apiHttpSecurityCustomizer.configureJsonUnauthorized(http);
        return http.build();
    }
}
