package com.crud.config.security;

import com.crud.security.JwtSecretKeyFactory;
import com.crud.security.servicejwt.ServiceJwtProperties;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
@Profile("rest")
public class ServiceJwtConfig {

    @Bean(name = "serviceJwtEncoder")
    JwtEncoder serviceJwtEncoder(ServiceJwtProperties properties, JwtSecretKeyFactory secretKeyFactory) {
        byte[] secretBytes = secretKeyFactory.secretBytes(
                properties.secret(), "app.security.service-jwt.secret");
        OctetSequenceKey jwk = new OctetSequenceKey.Builder(secretBytes)
                .algorithm(JWSAlgorithm.HS256)
                .keyID("service-jwt")
                .build();
        JWKSource<SecurityContext> source = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(source);
    }
}
