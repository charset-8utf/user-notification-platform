package com.platform.gateway.fallback;

import org.springframework.stereotype.Component;

@Component
public class NotificationServiceFallbackHandler extends GatewayFallbackTemplate {

    @Override
    protected String serviceName() {
        return "notification-service";
    }
}
