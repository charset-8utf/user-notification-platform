package com.platform.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.gateway.rate-limit")
public record GatewayRateLimitProperties(
        @DefaultValue Route auth,
        @DefaultValue Route userApi,
        @DefaultValue Route notificationLogs
) {

    public record Route(
            @DefaultValue("30") int replenishRate,
            @DefaultValue("60") int burstCapacity
    ) {
    }
}
