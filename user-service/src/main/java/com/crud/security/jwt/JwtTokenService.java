package com.crud.security.jwt;

import com.crud.config.JwtProperties;
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
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Profile("jwt")
public class JwtTokenService {

    private static final String ROLE_PREFIX = "ROLE_";

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;
    private final RefreshTokenStore refreshTokenStore;

    public JwtTokenService(JwtEncoder jwtEncoder, JwtProperties jwtProperties, RefreshTokenStore refreshTokenStore) {
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
        this.refreshTokenStore = refreshTokenStore;
    }

    public TokenPair issueTokenPair(UserDetails user) {
        String accessToken = encodeAccessToken(user);
        String refreshToken = issueRefreshToken(user);
        return new TokenPair(accessToken, refreshToken, jwtProperties.accessTokenTtl().getSeconds());
    }

    public TokenPair rotateRefreshToken(String refreshTokenId) {
        RefreshTokenRecord tokenRecord = refreshTokenStore.find(refreshTokenId)
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token недействителен или отозван"));
        refreshTokenStore.blacklist(refreshTokenId, jwtProperties.refreshTokenTtl());
        UserDetails user = toUserDetails(tokenRecord);
        return issueTokenPair(user);
    }

    public void revokeRefreshToken(String refreshTokenId) {
        refreshTokenStore.blacklist(refreshTokenId, jwtProperties.refreshTokenTtl());
    }

    private String issueRefreshToken(UserDetails user) {
        String tokenId = UUID.randomUUID().toString();
        List<String> roles = extractRoleNames(user);
        RefreshTokenRecord tokenRecord = new RefreshTokenRecord(user.getUsername(), roles);
        refreshTokenStore.store(tokenId, tokenRecord, jwtProperties.refreshTokenTtl());
        return tokenId;
    }

    private String encodeAccessToken(UserDetails user) {
        Instant now = Instant.now();
        List<String> roles = extractRoleNames(user);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.issuer())
                .subject(user.getUsername())
                .issuedAt(now)
                .expiresAt(now.plus(jwtProperties.accessTokenTtl()))
                .claim("roles", roles)
                .claim("typ", "access")
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    private static List<String> extractRoleNames(UserDetails user) {
        return user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(JwtTokenService::stripRolePrefix)
                .filter(Objects::nonNull)
                .toList();
    }

    private static UserDetails toUserDetails(RefreshTokenRecord tokenRecord) {
        var authorities = tokenRecord.roles().stream()
                .filter(Objects::nonNull)
                .map(role -> (GrantedAuthority) () -> toRoleAuthority(role))
                .collect(Collectors.toSet());
        return User.builder()
                .username(tokenRecord.username())
                .password("")
                .authorities(authorities)
                .build();
    }

    private static boolean hasRolePrefix(String value) {
        return value != null && value.startsWith(ROLE_PREFIX);
    }

    private static String stripRolePrefix(String authority) {
        if (authority == null) {
            return null;
        }
        return hasRolePrefix(authority) ? authority.substring(ROLE_PREFIX.length()) : authority;
    }

    private static String toRoleAuthority(String role) {
        return hasRolePrefix(role) ? role : ROLE_PREFIX + role;
    }

    public record TokenPair(String accessToken, String refreshToken, long expiresInSeconds) {
    }
}
