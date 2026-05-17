package com.crud.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;

@Configuration
@Profile("kafka")
@ConditionalOnProperty(name = "app.kafka.security.enabled", havingValue = "true")
public class KafkaSecurityConfiguration {

    public KafkaSecurityConfiguration(
            KafkaProperties kafkaProperties,
            KafkaSecurityProperties security,
            ResourceLoader resourceLoader
    ) {
        KafkaSecuritySupport.apply(kafkaProperties, security, resourceLoader);
    }
}
