package com.platform.config.backend;

import com.platform.config.config.NativeConfigServerProperties;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NativeConfigBackendStrategyTest {

    @Test
    void describesNativeBackendFromProperties() {
        NativeConfigServerProperties properties = new NativeConfigServerProperties(List.of("file:../config-repo"));
        NativeConfigBackendStrategy strategy = new NativeConfigBackendStrategy(properties);

        assertThat(strategy.profile()).isEqualTo("native");
        assertThat(strategy.repositoryDescription()).isEqualTo("Native: file:../config-repo");
    }
}
