package com.notification.support;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public final class ServiceJwtTestSupport {

    public static final String TEST_SECRET = "test-service-jwt-secret-for-tests-min-32b";
    public static final String ISSUER = "user-notification-platform";
    public static final String SUBJECT = "user-service";
    public static final String AUDIENCE = "notification-service";
    public static final String SCOPE = "notifications:write";

    private static final Instant CONTRACT_TOKEN_ISSUED_AT = Instant.parse("2026-05-30T00:00:00Z");

    /**
     * Фиксированный Bearer для Spring Cloud Contract (producer + stub-runner consumer).
     * TTL ~10 лет; secret/claims совпадают с {@code application-contract.yml}.
     */
    public static final String CONTRACT_AUTHORIZATION = "Bearer "
            + accessToken(
            TEST_SECRET,
            ISSUER,
            SUBJECT,
            AUDIENCE,
            SCOPE,
            CONTRACT_TOKEN_ISSUED_AT,
            Duration.ofDays(3650));

    private ServiceJwtTestSupport() {
    }

    public static String bearerToken() {
        return "Bearer " + accessToken();
    }

    public static String accessToken() {
        return accessToken(TEST_SECRET, ISSUER, SUBJECT, AUDIENCE, SCOPE, Duration.ofMinutes(5));
    }

    public static String accessToken(
            String secret,
            String issuer,
            String subject,
            String audience,
            String scope,
            Duration ttl
    ) {
        return accessToken(secret, issuer, subject, audience, scope, Instant.now(), ttl);
    }

    public static String accessToken(
            String secret,
            String issuer,
            String subject,
            String audience,
            String scope,
            Instant issuedAt,
            Duration ttl
    ) {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        OctetSequenceKey jwk = new OctetSequenceKey.Builder(secretBytes)
                .algorithm(JWSAlgorithm.HS256)
                .keyID("test-service-jwt")
                .build();
        var encoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(jwk)));
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(subject)
                .audience(List.of(audience))
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plus(ttl))
                .claim("scope", scope)
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
