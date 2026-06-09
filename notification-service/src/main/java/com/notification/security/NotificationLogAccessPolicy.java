package com.notification.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationLogAccessPolicy {

    private final JwtRoleSupport jwtRoleSupport;

    public void assertCanRead(String requestedEmail, Jwt jwt) {
        if (jwtRoleSupport.hasRole(jwt, "ADMIN")) {
            return;
        }
        String tokenEmail = jwt.getClaimAsString("email");
        if (tokenEmail == null || tokenEmail.isBlank() || !tokenEmail.equalsIgnoreCase(requestedEmail)) {
            throw new AccessDeniedException("Доступ к логам уведомлений только для своего email");
        }
    }
}
