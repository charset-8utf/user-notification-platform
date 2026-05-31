package com.platform.eureka.registry;

import com.netflix.eureka.registry.PeerAwareInstanceRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ServiceRegistryFacade {

    private final ObjectProvider<PeerAwareInstanceRegistry> registryProvider;

    public int registeredApplicationCount() {
        PeerAwareInstanceRegistry registry = registryProvider.getIfAvailable();
        if (registry == null) {
            return 0;
        }
        return registry.getApplications().getRegisteredApplications().size();
    }

    public int registeredInstanceCount() {
        PeerAwareInstanceRegistry registry = registryProvider.getIfAvailable();
        if (registry == null) {
            return 0;
        }
        return registry.getApplications().getRegisteredApplications().stream()
                .mapToInt(app -> app.getInstances().size())
                .sum();
    }
}
