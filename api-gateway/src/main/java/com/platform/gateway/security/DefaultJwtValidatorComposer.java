package com.platform.gateway.security;

import org.jspecify.annotations.Nullable;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DefaultJwtValidatorComposer implements JwtValidatorComposer {

    @Override
    public OAuth2TokenValidator<Jwt> compose(@Nullable String issuerUri, @Nullable String expectedIssuer) {
        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
        if (issuerUri != null && !issuerUri.isBlank()) {
            validators.add(JwtValidators.createDefaultWithIssuer(issuerUri));
        } else {
            validators.add(JwtValidators.createDefault());
            if (expectedIssuer != null && !expectedIssuer.isBlank()) {
                validators.add(new JwtClaimValidator<>("iss", expectedIssuer::equals));
            }
        }
        return new DelegatingOAuth2TokenValidator<>(validators);
    }
}
