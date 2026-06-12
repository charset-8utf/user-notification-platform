package com.notification.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtRoleSupport {

    private static final String ROLE_PREFIX = "ROLE_";
    private static final String ROLES_CLAIM = "roles";

    @Qualifier("userJwtGrantedAuthoritiesConverter")
    private final JwtGrantedAuthoritiesConverter defaultConverter;

    public Collection<GrantedAuthority> resolveAuthorities(Jwt jwt) {
        Collection<GrantedAuthority> fromRoles = defaultConverter.convert(jwt);
        if (!fromRoles.isEmpty()) {
            return fromRoles;
        }
        return rolesClaim(jwt).stream()
                .map(this::toRoleAuthority)
                .toList();
    }

    public boolean hasRole(Jwt jwt, String role) {
        Object roles = jwt.getClaim(ROLES_CLAIM);
        if (roles instanceof Iterable<?> iterable) {
            for (Object value : iterable) {
                if (role.equalsIgnoreCase(String.valueOf(value))) {
                    return true;
                }
            }
        }
        return false;
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
}
