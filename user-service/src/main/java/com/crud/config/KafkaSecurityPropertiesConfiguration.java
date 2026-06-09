package com.crud.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("kafka")
@EnableConfigurationProperties(KafkaSecurityProperties.class)
public class KafkaSecurityPropertiesConfiguration {
}
