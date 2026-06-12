package com.platform.config.backend;

import com.platform.config.config.GitConfigServerProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("git")
@RequiredArgsConstructor
public class GitConfigBackendStrategy implements ConfigBackendStrategy {

    private final GitConfigServerProperties properties;

    @Override
    public String profile() {
        return "git";
    }

    @Override
    public String repositoryDescription() {
        return properties.repositoryDescription();
    }
}
