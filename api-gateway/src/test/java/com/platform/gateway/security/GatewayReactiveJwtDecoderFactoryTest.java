package com.platform.gateway.security;

import com.platform.gateway.config.GatewayJwtProperties;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GatewayReactiveJwtDecoderFactoryTest {

    private final GatewayReactiveJwtDecoderFactory factory =
            new GatewayReactiveJwtDecoderFactory(new DefaultJwtValidatorComposer());

    @Test
    void createsHs256DecoderWhenSecretConfigured() {
        GatewayJwtProperties properties = new GatewayJwtProperties(
                "test-jwt-secret-for-gateway-tests-min-32b", "user-service", null, null);

        ReactiveJwtDecoder decoder = factory.create(properties);

        assertThat(decoder).isNotNull();
    }

    @Test
    void rejectsMissingSecretForLocalJwtMode() {
        GatewayJwtProperties properties = new GatewayJwtProperties(null, "user-service", null, null);

        assertThatThrownBy(() -> factory.create(properties))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("app.security.jwt.secret обязателен");
    }

    @Test
    void rejectsShortSecret() {
        GatewayJwtProperties properties = new GatewayJwtProperties("short", "user-service", null, null);

        assertThatThrownBy(() -> factory.create(properties))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32 байт");
    }
}
