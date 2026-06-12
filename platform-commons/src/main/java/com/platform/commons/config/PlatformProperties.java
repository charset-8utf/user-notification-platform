package com.platform.commons.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "platform")
public record PlatformProperties(
        @DefaultValue("default") String environment,
        @DefaultValue Logging logging,
        @DefaultValue OpenApi openapi,
        @DefaultValue Tracing tracing
) {

    public record Logging(@DefaultValue("true") boolean traceMdc) {
    }

    public record OpenApi(
            @DefaultValue("User Notification Platform API") String description,
            @DefaultValue("1.0.0") String version
    ) {
    }

    public record Tracing(@DefaultValue("false") boolean enabled) {
    }
}
