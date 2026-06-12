package com.platform.commons.openapi;

import com.platform.commons.config.PlatformProperties;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformOpenApiConfigurationTest {

    private final PlatformOpenApiConfiguration configuration = new PlatformOpenApiConfiguration() {
    };

    @Test
    void buildsOpenApiFromPropertiesAndApplicationName() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.application.name", "notification-service");
        PlatformProperties properties = new PlatformProperties(
                "default",
                new PlatformProperties.Logging(true),
                new PlatformProperties.OpenApi("Platform API", "2.0.0"),
                new PlatformProperties.Tracing(false));

        OpenAPI openApi = configuration.platformOpenApi(environment, properties);

        assertThat(openApi.getInfo().getTitle()).isEqualTo("notification-service");
        assertThat(openApi.getInfo().getDescription()).isEqualTo("Platform API");
        assertThat(openApi.getInfo().getVersion()).isEqualTo("2.0.0");
        assertThat(openApi.getComponents().getSecuritySchemes()).containsKeys("bearerAuth", "apiKeyAuth");
        assertThat(openApi.getSecurity()).hasSize(1);
    }
}
