package com.platform.commons.kafka;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;

/**
 * SASL_SSL для Kafka-клиентов ({@code app.kafka.security.enabled=true}, профиль {@code kafka}).
 * <p>
 */
@AutoConfiguration(after = KafkaAutoConfiguration.class)
@ConditionalOnClass(KafkaProperties.class)
@Profile("kafka")
@ConditionalOnProperty(name = "app.kafka.security.enabled", havingValue = "true")
public class KafkaSecurityAutoConfiguration {

    @Bean
    @ConditionalOnBean(KafkaProperties.class)
    KafkaSecuritySupport kafkaSecuritySupport(
            KafkaProperties kafkaProperties,
            KafkaSecurityProperties security,
            ResourceLoader resourceLoader
    ) {
        KafkaSecuritySupport support = new KafkaSecuritySupport();
        support.apply(kafkaProperties, security, resourceLoader);
        return support;
    }
}
