package com.crud.support;

import com.crud.security.servicejwt.ServiceJwtProperties;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

public final class ServiceJwtTestSupport {

    public static final String TEST_SECRET = "test-service-jwt-secret-for-tests-min-32b";

    private ServiceJwtTestSupport() {
    }

    public static ServiceJwtProperties properties(String secret) {
        return new ServiceJwtProperties(
                secret,
                "user-notification-platform",
                "user-service",
                "notification-service",
                "notifications:write",
                Duration.ofMinutes(5)
        );
    }

    public static JwtEncoder createEncoder(ServiceJwtProperties properties) {
        byte[] secretBytes = properties.secret().getBytes(StandardCharsets.UTF_8);
        OctetSequenceKey jwk = new OctetSequenceKey.Builder(secretBytes)
                .algorithm(JWSAlgorithm.HS256)
                .keyID("service-jwt-test")
                .build();
        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(jwk)));
    }
}
