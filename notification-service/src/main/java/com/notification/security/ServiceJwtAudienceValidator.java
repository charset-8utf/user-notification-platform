package com.notification.security;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ServiceJwtAudienceValidator {

    public boolean matches(Jwt jwt, String expectedAudience) {
        Object aud = jwt.getAudience();
        if (aud == null) {
            aud = jwt.getClaim("aud");
        }
        if (aud instanceof String audience) {
            return expectedAudience.equals(audience);
        }
        if (aud instanceof List<?> list) {
            return list.stream().anyMatch(expectedAudience::equals);
        }
        return false;
    }
}
