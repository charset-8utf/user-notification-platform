package com.platform.config.backend;

import com.platform.config.config.NativeConfigServerProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("native")
@RequiredArgsConstructor
public class NativeConfigBackendStrategy implements ConfigBackendStrategy {

    private final NativeConfigServerProperties properties;

    @Override
    public String profile() {
        return "native";
    }

    @Override
    public String repositoryDescription() {
        return properties.repositoryDescription();
    }
}
