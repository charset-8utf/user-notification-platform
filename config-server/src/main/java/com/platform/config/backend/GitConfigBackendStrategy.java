package com.platform.config.backend;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("git")
public class GitConfigBackendStrategy implements ConfigBackendStrategy {

    @Override
    public String profile() {
        return "git";
    }

    @Override
    public String repositoryDescription() {
        return "Git-backed config repository";
    }
}
