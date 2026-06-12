package com.platform.config.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigServerPropertiesBindingTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class);

    @Test
    void bindsNativeProperties() {
        contextRunner
                .withPropertyValues("spring.cloud.config.server.native.search-locations=file:./config-repo")
                .run(context -> {
                    NativeConfigServerProperties properties = context.getBean(NativeConfigServerProperties.class);
                    assertThat(properties.searchLocations()).containsExactly("file:./config-repo");
                    assertThat(properties.repositoryDescription()).isEqualTo("Native: file:./config-repo");
                });
    }

    @Test
    void bindsGitProperties() {
        contextRunner
                .withPropertyValues(
                        "spring.cloud.config.server.git.uri=file:///tmp/repo.git",
                        "spring.cloud.config.server.git.default-label=develop",
                        "spring.cloud.config.server.git.clone-on-start=false")
                .run(context -> {
                    GitConfigServerProperties properties = context.getBean(GitConfigServerProperties.class);
                    assertThat(properties.uri()).isEqualTo("file:///tmp/repo.git");
                    assertThat(properties.defaultLabel()).isEqualTo("develop");
                    assertThat(properties.cloneOnStart()).isFalse();
                    assertThat(properties.repositoryDescription()).isEqualTo("Git: file:///tmp/repo.git (label=develop)");
                });
    }

    @Configuration
    @EnableConfigurationProperties({NativeConfigServerProperties.class, GitConfigServerProperties.class})
    static class TestConfig {
    }
}
