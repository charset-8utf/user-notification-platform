package com.platform.config;

import com.platform.config.backend.ConfigBackendStrategy;
import com.platform.config.backend.NativeConfigBackendStrategy;
import com.platform.config.config.ConfigServerPropertiesConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigServerNativeProfileContextTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(ConfigServerPropertiesConfiguration.class, NativeConfigBackendStrategy.class)
            .withPropertyValues(
                    "spring.profiles.active=native",
                    "spring.cloud.config.server.native.search-locations=file:../config-repo");

    @Test
    void registersNativeBackendStrategyForNativeProfile() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ConfigBackendStrategy.class);
            assertThat(context.getBean(ConfigBackendStrategy.class)).isInstanceOf(NativeConfigBackendStrategy.class);
        });
    }
}
