package com.platform.gateway.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultJwtValidatorComposerTest {

    private final DefaultJwtValidatorComposer composer = new DefaultJwtValidatorComposer();

    @Test
    void composesDefaultValidatorsWithExpectedIssuer() {
        assertThat(composer.compose(null, "user-service")).isNotNull();
    }

    @Test
    void composesIssuerUriValidators() {
        assertThat(composer.compose("https://issuer.example.com", "user-service")).isNotNull();
    }
}
