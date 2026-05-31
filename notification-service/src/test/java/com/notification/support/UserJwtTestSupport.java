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

public final class UserJwtTestSupport {

    public static final String TEST_SECRET = "test-jwt-secret-for-notification-read-min-32b";

    private UserJwtTestSupport() {
    }

    public static String bearerToken(String email) {
        return "Bearer " + accessToken(email, List.of("USER"));
    }

    public static String adminBearerToken(String email) {
        return "Bearer " + accessToken(email, List.of("ADMIN"));
    }

    private static String accessToken(String email, List<String> roles) {
        byte[] secretBytes = TEST_SECRET.getBytes(StandardCharsets.UTF_8);
        OctetSequenceKey jwk = new OctetSequenceKey.Builder(secretBytes)
                .algorithm(JWSAlgorithm.HS256)
                .keyID("notification-user-jwt-test")
                .build();
        var encoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(jwk)));
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("user-service")
                .subject("user1")
                .claim("roles", roles)
                .claim("typ", "access")
                .claim("email", email)
                .issuedAt(now)
                .expiresAt(now.plus(Duration.ofMinutes(15)))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
