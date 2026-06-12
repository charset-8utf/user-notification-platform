package com.platform.commons.kafka;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(KafkaSecurityProperties.class)
public class KafkaSecurityPropertiesConfiguration {
}
