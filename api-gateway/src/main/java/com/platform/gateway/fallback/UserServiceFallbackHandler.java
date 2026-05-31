package com.platform.gateway.fallback;

import org.springframework.stereotype.Component;

@Component
public class UserServiceFallbackHandler extends GatewayFallbackTemplate {

    @Override
    protected String serviceName() {
        return "user-service";
    }
}
