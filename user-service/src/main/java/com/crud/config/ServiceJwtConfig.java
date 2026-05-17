package com.crud.config;

import com.crud.security.servicejwt.ServiceJwtProperties;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.nio.charset.StandardCharsets;

@Configuration
@Profile("rest")
@EnableConfigurationProperties(ServiceJwtProperties.class)
public class ServiceJwtConfig {

    @Bean(name = "serviceJwtEncoder")
    JwtEncoder serviceJwtEncoder(ServiceJwtProperties properties) {
        byte[] secretBytes = secretBytes(properties);
        OctetSequenceKey jwk = new OctetSequenceKey.Builder(secretBytes)
                .algorithm(JWSAlgorithm.HS256)
                .keyID("service-jwt")
                .build();
        JWKSource<SecurityContext> source = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(source);
    }

    private static byte[] secretBytes(ServiceJwtProperties properties) {
        byte[] bytes = properties.secret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("app.security.service-jwt.secret must be at least 32 bytes for HS256");
        }
        return bytes;
    }
}
