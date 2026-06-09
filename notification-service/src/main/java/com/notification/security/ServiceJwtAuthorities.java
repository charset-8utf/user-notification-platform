package com.notification.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ServiceJwtAuthorities {

    private final ServiceJwtProperties properties;

    public String writeScopeAuthority() {
        return "SCOPE_" + properties.scope();
    }
}
