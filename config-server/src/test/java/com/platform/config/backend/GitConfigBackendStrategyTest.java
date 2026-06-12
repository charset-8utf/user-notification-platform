package com.platform.config.backend;

import com.platform.config.config.GitConfigServerProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GitConfigBackendStrategyTest {

    @Test
    void describesGitBackendFromProperties() {
        GitConfigServerProperties properties = new GitConfigServerProperties(
                "file:///tmp/config-repo.git", "main", true);
        GitConfigBackendStrategy strategy = new GitConfigBackendStrategy(properties);

        assertThat(strategy.profile()).isEqualTo("git");
        assertThat(strategy.repositoryDescription()).isEqualTo("Git: file:///tmp/config-repo.git (label=main)");
    }
}
