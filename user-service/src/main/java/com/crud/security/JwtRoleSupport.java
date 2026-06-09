package com.crud.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null) {
            Object realmRoles = realmAccess.get("roles");
            if (realmRoles instanceof Collection<?> collection) {
                return collection.stream().map(Object::toString).toList();
            }
        }
        return Collections.emptyList();
    }

    private GrantedAuthority toRoleAuthority(String role) {
        if (role.startsWith(ROLE_PREFIX)) {
            return new SimpleGrantedAuthority(role);
        }
        return new SimpleGrantedAuthority(ROLE_PREFIX + role.toUpperCase(Locale.ROOT));
    }

    private JwtGrantedAuthoritiesConverter newDefaultConverter() {
        JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
        converter.setAuthorityPrefix(ROLE_PREFIX);
        converter.setAuthoritiesClaimName(ROLES_CLAIM);
        return converter;
    }
}
