package com.platform.commons.kafka;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaSecurityAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    KafkaSecurityPropertiesConfiguration.class,
                    KafkaSecurityAutoConfiguration.class))
            .withBean(KafkaProperties.class, KafkaProperties::new)
            .withBean(ResourceLoader.class, DefaultResourceLoader::new)
            .withPropertyValues(
                    "spring.profiles.active=kafka",
                    "app.kafka.security.enabled=true",
                    "app.kafka.security.username=platform-service",
                    "app.kafka.security.password=secret",
                    "app.kafka.security.trust-store=classpath:kafka-truststore.p12",
                    "app.kafka.security.trust-store-password=changeit",
                    "app.kafka.security.trust-store-type=PKCS12",
                    "app.kafka.security.endpoint-identification-algorithm=");

    @Test
    void appliesKafkaSecurityWhenEnabled() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(KafkaSecuritySupport.class);
            KafkaProperties kafkaProperties = context.getBean(KafkaProperties.class);
            assertThat(kafkaProperties.getProperties()).containsEntry("security.protocol", "SASL_SSL");
        });
    }

    @Test
    void skipsWhenSecurityDisabled() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(KafkaSecurityAutoConfiguration.class))
                .withBean(KafkaProperties.class, KafkaProperties::new)
                .withPropertyValues(
                        "spring.profiles.active=kafka",
                        "app.kafka.security.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(KafkaSecuritySupport.class));
    }
}
