package com.platform.config.info;

import com.platform.config.backend.ConfigBackendStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ConfigBackendInfoContributor implements InfoContributor {

    private final ConfigBackendStrategyFactory backendStrategyFactory;

    @Override
    public void contribute(Info.Builder builder) {
        var strategy = backendStrategyFactory.activeStrategy();
        builder.withDetail("configBackend", Map.of(
                "profile", strategy.profile(),
                "repository", strategy.repositoryDescription()));
    }
}
