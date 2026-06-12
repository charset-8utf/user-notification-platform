package com.platform.bff.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.bff.api")
public record BffApiProperties(
        @DefaultValue Actuator actuator,
        @DefaultValue Bff bff
) {

    public record Actuator(
            @DefaultValue("/actuator/health") String health,
            @DefaultValue("/actuator/info") String info
    ) {
    }

    public record Bff(
            @DefaultValue("/bff/**") String authenticatedPath
    ) {
    }
}
