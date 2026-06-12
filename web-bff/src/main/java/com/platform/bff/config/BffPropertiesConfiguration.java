package com.platform.bff.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        BffClientProperties.class,
        BffApiProperties.class,
        BffJwtProperties.class
})
public class BffPropertiesConfiguration {
}
