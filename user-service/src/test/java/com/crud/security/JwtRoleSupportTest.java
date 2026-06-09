package com.crud.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtRoleSupportTest {

    private final JwtRoleSupport support = new JwtRoleSupport();

    @Test
    void resolveAuthorities_fromRolesClaim() {
        Jwt jwt = jwtWithClaims(Map.of("roles", List.of("USER")));

        var authorities = support.resolveAuthorities(jwt);

        assertThat(authorities).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }

    @Test
    void resolveAuthorities_fromRealmAccessRoles() {
        Jwt jwt = jwtWithClaims(Map.of("realm_access", Map.of("roles", List.of("admin"))));

        var authorities = support.resolveAuthorities(jwt);

        assertThat(authorities).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void resolveAuthorities_preservesExistingRolePrefixFromRealmAccess() {
        Jwt jwt = jwtWithClaims(Map.of("realm_access", Map.of("roles", List.of("ROLE_ADMIN"))));

        var authorities = support.resolveAuthorities(jwt);

        assertThat(authorities).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void resolveAuthorities_returnsEmptyWhenNoRoles() {
        Jwt jwt = jwtWithClaims(Map.of("sub", "user"));

        var authorities = support.resolveAuthorities(jwt);

        assertThat(authorities).isEmpty();
    }

    private static Jwt jwtWithClaims(Map<String, Object> claims) {
        var builder = Jwt.withTokenValue("test-token").header("alg", "none");
        claims.forEach(builder::claim);
        return builder.build();
    }
}
