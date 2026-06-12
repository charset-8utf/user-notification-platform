package com.platform.bff.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BffJwtPropertiesTest {

    @Test
    void requireSecret_returnsConfiguredValue() {
        BffJwtProperties properties = new BffJwtProperties("test-jwt-secret-for-bff-webmvc-tests-min-32b");

        assertThat(properties.requireSecret()).isEqualTo("test-jwt-secret-for-bff-webmvc-tests-min-32b");
    }

    @Test
    void requireSecret_rejectsBlankValue() {
        BffJwtProperties properties = new BffJwtProperties("  ");

        assertThatThrownBy(properties::requireSecret)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("app.security.jwt.secret is required");
    }
}
