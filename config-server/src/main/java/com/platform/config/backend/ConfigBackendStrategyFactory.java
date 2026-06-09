package com.platform.config.backend;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConfigBackendStrategyFactory {

    private final ConfigBackendStrategy activeStrategy;

    public ConfigBackendStrategyFactory(List<ConfigBackendStrategy> strategies) {
        if (strategies.isEmpty()) {
            throw new IllegalStateException("No ConfigBackendStrategy beans registered");
        }
        this.activeStrategy = strategies.getFirst();
    }

    public ConfigBackendStrategy activeStrategy() {
        return activeStrategy;
    }
}
