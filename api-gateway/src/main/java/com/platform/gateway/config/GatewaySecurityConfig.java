package com.platform.gateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtGrantedAuthoritiesConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@Profile("cloud & !cloud-it")
@RequiredArgsConstructor
public class GatewaySecurityConfig {

    private final GatewayApiProperties apiProperties;

    @Bean
    SecurityWebFilterChain gatewaySecurityFilterChain(ServerHttpSecurity http, ReactiveJwtDecoder jwtDecoder) {
        ReactiveJwtAuthenticationConverter jwtConverter = new ReactiveJwtAuthenticationConverter();
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("roles");
        authoritiesConverter.setAuthorityPrefix("ROLE_");
        jwtConverter.setJwtGrantedAuthoritiesConverter(
                new ReactiveJwtGrantedAuthoritiesConverterAdapter(authoritiesConverter));

        GatewayApiProperties.Actuator actuator = apiProperties.actuator();
        GatewayApiProperties.Auth auth = apiProperties.auth();
        GatewayApiProperties.Admin admin = apiProperties.admin();
        GatewayApiProperties.UserApi user = apiProperties.user();
        GatewayApiProperties.NotificationApi notification = apiProperties.notification();

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(actuator.health(), actuator.info(), actuator.prometheus()).permitAll()
                        .pathMatchers(HttpMethod.POST, auth.login(), auth.refresh()).permitAll()
                        .pathMatchers(HttpMethod.POST, admin.roleAssign(), admin.roleRemove()).hasRole("ADMIN")
                        .pathMatchers(user.users(), user.profiles(), user.roles()).authenticated()
                        .pathMatchers(HttpMethod.GET, notification.logs()).authenticated()
                        .pathMatchers(notification.denyAll()).denyAll()
                        .anyExchange().denyAll())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtDecoder(jwtDecoder)
                                .jwtAuthenticationConverter(jwtConverter)))
                .build();
    }
}
