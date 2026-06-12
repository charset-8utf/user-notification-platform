package com.platform.gateway.security;

import org.jspecify.annotations.Nullable;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;

public interface JwtValidatorComposer {

    OAuth2TokenValidator<Jwt> compose(@Nullable String issuerUri, @Nullable String expectedIssuer);
}
