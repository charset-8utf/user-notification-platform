package com.platform.bff.security;

import com.platform.bff.config.BffJwtProperties;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BffJwtDecoderFactoryTest {

    private final BffJwtDecoderFactory factory = new BffJwtDecoderFactory();

    @Test
    void createsHs256DecoderWhenSecretConfigured() {
        BffJwtProperties properties = new BffJwtProperties("test-jwt-secret-for-bff-webmvc-tests-min-32b");

        JwtDecoder decoder = factory.create(properties);

        assertThat(decoder).isNotNull();
    }

    @Test
    void rejectsMissingSecret() {
        BffJwtProperties properties = new BffJwtProperties(null);

        assertThatThrownBy(() -> factory.create(properties))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("app.security.jwt.secret is required");
    }

    @Test
    void rejectsShortSecret() {
        BffJwtProperties properties = new BffJwtProperties("short");

        assertThatThrownBy(() -> factory.create(properties))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 32 bytes");
    }
}
