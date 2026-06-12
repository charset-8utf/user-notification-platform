package com.platform.commons.observability;

import com.platform.commons.config.PlatformProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlatformEnvironmentTagResolver implements EnvironmentTagResolver {

    private final PlatformProperties properties;
    private final Environment environment;

    @Override
    public String resolve() {
        String configured = properties.environment();
        if (!configured.isBlank() && !"default".equals(configured)) {
            return configured;
        }
        return environment.getProperty("spring.profiles.active", "default");
    }
}
