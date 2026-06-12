package com.crud.security.jwt;

import com.crud.config.security.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Profile("jwt")
@RequiredArgsConstructor
public class JwtTokenService {

    private static final String ROLE_PREFIX = "ROLE_";

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;
    private final RefreshTokenStore refreshTokenStore;

    public TokenPair issueTokenPair(UserDetails user) {
        return issueTokenPair(user, null);
    }

    public TokenPair issueTokenPair(UserDetails user, @Nullable String email) {
        String accessToken = encodeAccessToken(user, email);
        String refreshToken = issueRefreshToken(user, email);
        return new TokenPair(accessToken, refreshToken, jwtProperties.accessTokenTtl().getSeconds());
    }

    public TokenPair rotateRefreshToken(String refreshTokenId) {
        RefreshTokenRecord tokenRecord = refreshTokenStore.find(refreshTokenId)
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token недействителен или отозван"));
        refreshTokenStore.blacklist(refreshTokenId, jwtProperties.refreshTokenTtl());
        UserDetails user = toUserDetails(tokenRecord);
        return issueTokenPair(user, tokenRecord.email());
    }

    public void revokeRefreshToken(String refreshTokenId) {
        refreshTokenStore.blacklist(refreshTokenId, jwtProperties.refreshTokenTtl());
    }

    private String issueRefreshToken(UserDetails user, @Nullable String email) {
        String tokenId = UUID.randomUUID().toString();
        List<String> roles = extractRoleNames(user);
        RefreshTokenRecord tokenRecord = new RefreshTokenRecord(user.getUsername(), roles, email);
        refreshTokenStore.store(tokenId, tokenRecord, jwtProperties.refreshTokenTtl());
        return tokenId;
    }

    private String encodeAccessToken(UserDetails user, @Nullable String email) {
        Instant now = Instant.now();
        List<String> roles = extractRoleNames(user);

        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .issuer(jwtProperties.issuer())
                .subject(user.getUsername())
                .issuedAt(now)
                .expiresAt(now.plus(jwtProperties.accessTokenTtl()))
                .claim("roles", roles)
                .claim("typ", "access");
        if (email != null && !email.isBlank()) {
            claimsBuilder.claim("email", email);
        }
        JwtClaimsSet claims = claimsBuilder.build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    private List<String> extractRoleNames(UserDetails user) {
        return user.getAuthorities().stream()
                .flatMap(granted -> Optional.ofNullable(granted.getAuthority()).stream().map(this::stripRolePrefix))
                .toList();
    }

    private UserDetails toUserDetails(RefreshTokenRecord tokenRecord) {
        var authorities = tokenRecord.roles().stream()
                .map(role -> (GrantedAuthority) () -> toRoleAuthority(role))
                .collect(Collectors.toSet());
        return User.builder()
                .username(tokenRecord.username())
                .password("")
                .authorities(authorities)
                .build();
    }

    private boolean hasRolePrefix(String value) {
        return value.startsWith(ROLE_PREFIX);
    }

    private String stripRolePrefix(String authority) {
        return hasRolePrefix(authority) ? authority.substring(ROLE_PREFIX.length()) : authority;
    }

    private String toRoleAuthority(String role) {
        return hasRolePrefix(role) ? role : ROLE_PREFIX + role;
    }

    public record TokenPair(String accessToken, String refreshToken, long expiresInSeconds) {
    }
}
