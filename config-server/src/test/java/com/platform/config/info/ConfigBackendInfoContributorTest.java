package com.platform.config.info;

import com.platform.config.backend.ConfigBackendStrategy;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.info.Info;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConfigBackendInfoContributorTest {

    @Test
    void contributesActiveBackendDetails() {
        ConfigBackendStrategy strategy = mock(ConfigBackendStrategy.class);
        when(strategy.profile()).thenReturn("native");
        when(strategy.repositoryDescription()).thenReturn("Native: file:../config-repo");

        ConfigBackendInfoContributor contributor = new ConfigBackendInfoContributor(strategy);
        Info.Builder builder = new Info.Builder();
        contributor.contribute(builder);
        Info info = builder.build();

        @SuppressWarnings("unchecked")
        Map<String, Object> backend = (Map<String, Object>) info.getDetails().get("configBackend");
        assertThat(backend)
                .containsEntry("profile", "native")
                .containsEntry("repository", "Native: file:../config-repo");
    }
}
