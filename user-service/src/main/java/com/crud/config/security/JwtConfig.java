package com.crud.config.security;

import com.crud.security.JwtRoleSupport;
import com.crud.security.JwtSecretKeyFactory;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.util.StringUtils;

@Configuration
@Profile("jwt")
public class JwtConfig {

    @Bean
    JwtEncoder jwtEncoder(JwtProperties properties, JwtSecretKeyFactory secretKeyFactory) {
        byte[] secretBytes = secretKeyFactory.secretBytes(
                properties.secret(), "app.security.jwt.secret");
        OctetSequenceKey jwk = new OctetSequenceKey.Builder(secretBytes)
                .algorithm(JWSAlgorithm.HS256)
                .keyID("user-service-symmetric")
                .build();
        JWKSource<SecurityContext> source = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(source);
    }

    @Bean
    JwtDecoder jwtDecoder(JwtProperties properties, JwtSecretKeyFactory secretKeyFactory) {
        if (properties.oidcEnabled()) {
            String jwkSetUri = properties.jwkSetUri();
            if (StringUtils.hasText(jwkSetUri)) {
                return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
            }
            return NimbusJwtDecoder.withIssuerLocation(properties.requiredIssuerUri()).build();
        }
        return NimbusJwtDecoder.withSecretKey(
                secretKeyFactory.secretKey(properties.secret(), "app.security.jwt.secret")).build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter(JwtRoleSupport jwtRoleSupport) {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwtRoleSupport::resolveAuthorities);
        converter.setPrincipalClaimName("sub");
        return converter;
    }
}
