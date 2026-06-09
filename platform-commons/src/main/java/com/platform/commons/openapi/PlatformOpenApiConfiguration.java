package com.platform.commons.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public abstract class PlatformOpenApiConfiguration {

    @Bean
    public OpenAPI platformOpenApi(
            @Value("${spring.application.name:platform-service}") String serviceName
    ) {
        final String bearerScheme = "bearerAuth";
        final String apiKeyScheme = "apiKeyAuth";
        return new OpenAPI()
                .info(new Info()
                        .title(serviceName)
                        .description("User Notification Platform API")
                        .version("1.0.0"))
                .components(new Components()
                        .addSecuritySchemes(bearerScheme, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT"))
                        .addSecuritySchemes(apiKeyScheme, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-API-Key")))
                .addSecurityItem(new SecurityRequirement().addList(bearerScheme));
    }
}
