package com.platform.config.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

@ConfigurationProperties(prefix = "spring.cloud.config.server.native")
public record NativeConfigServerProperties(
        @DefaultValue("file:../config-repo") List<String> searchLocations
) {

    public String repositoryDescription() {
        if (searchLocations.isEmpty()) {
            return "Native filesystem config-repo";
        }
        return "Native: " + String.join(", ", searchLocations);
    }
}
