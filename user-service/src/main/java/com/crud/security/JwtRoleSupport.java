package com.crud.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class JwtRoleSupport {

    private static final String ROLE_PREFIX = "ROLE_";
    private static final String ROLES_CLAIM = "roles";

    private final JwtGrantedAuthoritiesConverter defaultConverter = newDefaultConverter();

    public Collection<GrantedAuthority> resolveAuthorities(Jwt jwt) {
        Collection<GrantedAuthority> fromRoles = defaultConverter.convert(jwt);
        if (!fromRoles.isEmpty()) {
            return fromRoles;
        }
        return rolesClaim(jwt).stream()
                .map(this::toRoleAuthority)
                .toList();
    }

    private List<String> rolesClaim(Jwt jwt) {
        Object roles = jwt.getClaim(ROLES_CLAIM);
        if (roles instanceof Collection<?> collection) {
            return collection.stream().map(Object::toString).toList();
        }
        return Collections.emptyList();
    }

    private GrantedAuthority toRoleAuthority(String role) {
        return new SimpleGrantedAuthority(role.startsWith(ROLE_PREFIX) ? role : ROLE_PREFIX + role);
    }

    private JwtGrantedAuthoritiesConverter newDefaultConverter() {
        JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
        converter.setAuthorityPrefix(ROLE_PREFIX);
        converter.setAuthoritiesClaimName(ROLES_CLAIM);
        return converter;
    }
}
