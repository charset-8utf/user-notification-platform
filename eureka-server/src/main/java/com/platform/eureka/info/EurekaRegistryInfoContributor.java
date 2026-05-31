package com.platform.eureka.info;

import com.platform.eureka.registry.ServiceRegistryFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class EurekaRegistryInfoContributor implements InfoContributor {

    private final ServiceRegistryFacade serviceRegistryFacade;

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("eurekaRegistry", Map.of(
                "applications", serviceRegistryFacade.registeredApplicationCount(),
                "instances", serviceRegistryFacade.registeredInstanceCount()));
    }
}
