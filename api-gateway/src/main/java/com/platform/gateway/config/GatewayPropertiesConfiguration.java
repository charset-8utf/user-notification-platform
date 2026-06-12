package com.platform.gateway.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        GatewayJwtProperties.class,
        GatewayApiProperties.class,
        GatewayRateLimitProperties.class,
        GatewaySslProperties.class
})
public class GatewayPropertiesConfiguration {
}
