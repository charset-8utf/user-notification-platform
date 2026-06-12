package com.platform.config.info;

import com.platform.config.backend.ConfigBackendStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ConfigBackendInfoContributor implements InfoContributor {

    private final ConfigBackendStrategy activeBackendStrategy;

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("configBackend", Map.of(
                "profile", activeBackendStrategy.profile(),
                "repository", activeBackendStrategy.repositoryDescription()));
    }
}
