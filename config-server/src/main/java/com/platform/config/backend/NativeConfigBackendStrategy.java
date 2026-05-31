package com.platform.config.backend;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("native")
public class NativeConfigBackendStrategy implements ConfigBackendStrategy {

    @Override
    public String profile() {
        return "native";
    }

    @Override
    public String repositoryDescription() {
        return "Native filesystem config-repo";
    }
}
