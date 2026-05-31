package com.crud.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

@Configuration
@ConditionalOnProperty(name = "app.kafka.security.enabled", havingValue = "true")
@EnableConfigurationProperties(KafkaSecurityProperties.class)
public class KafkaSecurityConfiguration {

    public KafkaSecurityConfiguration(
            KafkaProperties kafkaProperties,
            KafkaSecurityProperties security,
            ResourceLoader resourceLoader,
            KafkaSecurityPropertiesApplier kafkaSecurityPropertiesApplier
    ) {
        kafkaSecurityPropertiesApplier.apply(kafkaProperties, security, resourceLoader);
    }
}
