package com.platform.gateway.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Configuration
@Profile("cloud & !cloud-it")
@EnableConfigurationProperties(GatewayJwtProperties.class)
public class GatewayJwtDecoderConfiguration {

    @Bean
    ReactiveJwtDecoder reactiveJwtDecoder(GatewayJwtProperties properties) {
        if (properties.oidcEnabled()) {
            NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder
                    .withIssuerLocation(properties.issuerUri())
                    .build();
            decoder.setJwtValidator(composeValidators(properties.issuerUri(), properties.issuer()));
            return decoder;
        }
        if (properties.secret() == null || properties.secret().isBlank()) {
            throw new IllegalStateException("app.security.jwt.secret is required when issuer-uri is not set");
        }
        byte[] secretBytes = properties.secret().getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException("app.security.jwt.secret must be at least 32 bytes for HS256");
        }
        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder
                .withSecretKey(new SecretKeySpec(secretBytes, "HmacSHA256"))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(composeValidators(null, properties.issuer()));
        return decoder;
    }

    private static OAuth2TokenValidator<Jwt> composeValidators(String issuerUri, String expectedIssuer) {
        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
        if (issuerUri != null && !issuerUri.isBlank()) {
            validators.add(JwtValidators.createDefaultWithIssuer(issuerUri));
        } else {
            validators.add(JwtValidators.createDefault());
        }
        if (expectedIssuer != null && !expectedIssuer.isBlank()) {
            validators.add(new JwtClaimValidator<>("iss", expectedIssuer::equals));
        }
        return new DelegatingOAuth2TokenValidator<>(validators);
    }
}
