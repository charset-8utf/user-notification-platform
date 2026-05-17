package com.crud.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitKeyResolverTest {

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void usesJwtSubjectFromSecurityContext() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("alice")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(jwt, null, List.of()));

        String key = RateLimitKeyResolver.resolve(new MockHttpServletRequest());

        assertThat(key).isEqualTo("sub:alice");
    }

    @Test
    void usesUsernameWhenNotJwtPrincipal() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "bob", null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        String key = RateLimitKeyResolver.resolve(new MockHttpServletRequest());

        assertThat(key).isEqualTo("user:bob");
    }

    @Test
    void parsesSubFromBearerWithoutSecurityContext() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + carolBearerToken());

        assertThat(RateLimitKeyResolver.resolve(request)).isEqualTo("sub:carol");
    }

    @Test
    void fallsBackToIpWhenAnonymous() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("203.0.113.10");

        assertThat(RateLimitKeyResolver.resolve(request)).isEqualTo("ip:203.0.113.10");
    }

    private static String carolBearerToken() {
        String header = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"HS256\"}".getBytes(StandardCharsets.UTF_8));
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"sub\":\"carol\"}".getBytes(StandardCharsets.UTF_8));
        return header + "." + payload + ".sig";
    }
}
