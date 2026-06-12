package com.crud.config.security;

import com.crud.security.SecurityJsonErrorWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApiHttpSecurityCustomizer {

    private final SecurityJsonErrorWriter securityJsonErrorWriter;

    public void configureSecurityHeaders(HttpSecurity http) {
        http.headers(headers -> headers
                .contentTypeOptions(Customizer.withDefaults())
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                .contentSecurityPolicy(csp -> csp
                        .policyDirectives("default-src 'none'; frame-ancestors 'none'")));
    }

    public void configureJsonUnauthorized(HttpSecurity http) {
        http.exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
            log.warn("Попытка неавторизованного доступа: {} {}", request.getMethod(), request.getRequestURI());
            securityJsonErrorWriter.writeUnauthorized(response, request.getRequestURI());
        }));
    }
}
