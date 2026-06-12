package com.crud.config.security;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class ApiAuthorizationRules {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_USER = "USER";

    public void configure(
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
}
