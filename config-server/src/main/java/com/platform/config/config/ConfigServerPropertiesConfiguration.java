package com.platform.config.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        NativeConfigServerProperties.class,
        GitConfigServerProperties.class
})
public class ConfigServerPropertiesConfiguration {
}
