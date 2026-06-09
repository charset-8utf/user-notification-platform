package com.crud.security.jwt;

import com.crud.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenServiceTest {

    private static final String SECRET = "test-jwt-secret-for-unit-tests-min-32b";

    @Mock
    private RefreshTokenStore refreshTokenStore;

    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        byte[] secretBytes = SECRET.getBytes(StandardCharsets.UTF_8);
        OctetSequenceKey jwk = new OctetSequenceKey.Builder(secretBytes)
                .algorithm(JWSAlgorithm.HS256)
                .keyID("test-key")
                .build();
        JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(jwk)));
        JwtDecoder decoder = NimbusJwtDecoder.withSecretKey(new SecretKeySpec(secretBytes, "HmacSHA256")).build();
        JwtProperties properties = new JwtProperties(
                SECRET, "user-service", null, null, Duration.ofMinutes(15), Duration.ofDays(1));
        jwtTokenService = new JwtTokenService(encoder, properties, refreshTokenStore);
        assertThat(decoder).isNotNull();
    }

    @Test
    void issueTokenPair_storesRefreshAndReturnsTokens() {
        var user = User.builder()
                .username("admin")
                .password("x")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .build();

        var pair = jwtTokenService.issueTokenPair(user);

        assertThat(pair.accessToken()).isNotBlank();
        assertThat(pair.refreshToken()).isNotBlank();
        verify(refreshTokenStore).store(eq(pair.refreshToken()), any(RefreshTokenRecord.class), eq(Duration.ofDays(1)));
    }

    @Test
    void rotateRefreshToken_invalidatesOldAndIssuesNew() {
        RefreshTokenRecord tokenRecord = new RefreshTokenRecord("admin", List.of("ADMIN"));
        when(refreshTokenStore.find("old")).thenReturn(Optional.of(tokenRecord));

        var pair = jwtTokenService.rotateRefreshToken("old");

        assertThat(pair.accessToken()).isNotBlank();
        verify(refreshTokenStore).blacklist("old", Duration.ofDays(1));
    }

    @Test
    void rotateRefreshToken_missing_throws() {
        when(refreshTokenStore.find("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> jwtTokenService.rotateRefreshToken("missing"))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }
}
