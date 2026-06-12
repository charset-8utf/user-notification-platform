package com.platform.commons.openapi;

import com.platform.commons.config.PlatformProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * Facade: базовая OpenAPI-схема для сервисов платформы.
 */
public abstract class PlatformOpenApiConfiguration {

    private static final String BEARER_SCHEME = "bearerAuth";
    private static final String API_KEY_SCHEME = "apiKeyAuth";

    @Bean
    public OpenAPI platformOpenApi(Environment environment, PlatformProperties properties) {
        String serviceName = environment.getProperty("spring.application.name", "platform-service");
        PlatformProperties.OpenApi openApi = properties.openapi();
        return new OpenAPI()
                .info(new Info()
                        .title(serviceName)
                        .description(openApi.description())
                        .version(openApi.version()))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT"))
                        .addSecuritySchemes(API_KEY_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-API-Key")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }
}
