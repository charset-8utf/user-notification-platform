package com.crud.security.servicejwt;

import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Выдаёт и кэширует короткоживущий JWT для вызовов notification-service.
 */
@Component
@Profile("rest")
public class ServiceJwtTokenProvider {

    private final JwtEncoder serviceJwtEncoder;
    private final ServiceJwtProperties properties;
    private final AtomicReference<CachedToken> cached = new AtomicReference<>();

    public ServiceJwtTokenProvider(JwtEncoder serviceJwtEncoder, ServiceJwtProperties properties) {
        this.serviceJwtEncoder = serviceJwtEncoder;
        this.properties = properties;
    }

    public String getToken() {
        Instant refreshAfter = Instant.now().plusSeconds(30);
        CachedToken current = cached.get();
        if (isValid(current, refreshAfter)) {
            return current.value();
        }
        synchronized (this) {
            current = cached.get();
            if (isValid(current, refreshAfter)) {
                return current.value();
            }
            CachedToken issued = issueToken();
            cached.set(issued);
            return issued.value();
        }
    }

    private static boolean isValid(CachedToken token, Instant refreshAfter) {
        return token != null && token.validUntil().isAfter(refreshAfter);
    }

    private CachedToken issueToken() {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(properties.tokenTtl());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.issuer())
                .subject(properties.subject())
                .audience(List.of(properties.audience()))
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("scope", properties.scope())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = serviceJwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new CachedToken(token, expiresAt.minusSeconds(5));
    }

    private record CachedToken(String value, Instant validUntil) {
    }
}
