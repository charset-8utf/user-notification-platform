package com.notification.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.Objects;

final class ApiKeyAuthenticationToken extends AbstractAuthenticationToken {

    private static final String PRINCIPAL = "api-key-client";

    private final String apiKey;

    ApiKeyAuthenticationToken(String apiKey, String writeScopeAuthority) {
        super(AuthorityUtils.createAuthorityList(writeScopeAuthority));
        this.apiKey = apiKey;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return apiKey;
    }

    @Override
    public Object getPrincipal() {
        return PRINCIPAL;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ApiKeyAuthenticationToken that)) {
            return false;
        }
        if (!super.equals(other)) {
            return false;
        }
        return apiKey.equals(that.apiKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), apiKey);
    }
}
