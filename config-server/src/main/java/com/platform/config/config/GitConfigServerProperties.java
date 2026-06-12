package com.platform.config.config;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "spring.cloud.config.server.git")
public record GitConfigServerProperties(
        @Nullable String uri,
        @DefaultValue("main") String defaultLabel,
        @DefaultValue("true") boolean cloneOnStart
) {

    public String repositoryDescription() {
        if (uri == null || uri.isBlank()) {
            return "Git-backed config repository";
        }
        return "Git: " + uri + " (label=" + defaultLabel + ")";
    }
}
