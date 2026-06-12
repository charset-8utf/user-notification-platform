package com.platform.commons.kafka;

import org.junit.jupiter.api.Test;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KafkaSecuritySupportTest {

    private static final ResourceLoader RESOURCE_LOADER = new DefaultResourceLoader();
    private final KafkaSecuritySupport kafkaSecuritySupport = new KafkaSecuritySupport();

    @Test
    void appliesSaslSslProperties() {
        KafkaProperties kafkaProperties = new KafkaProperties();
        KafkaSecurityProperties security = new KafkaSecurityProperties(
                true,
                "platform-service",
                "secret",
                "classpath:kafka-truststore.p12",
                "changeit",
                "PKCS12",
                ""
        );
        kafkaSecuritySupport.apply(kafkaProperties, security, RESOURCE_LOADER);

        var props = kafkaProperties.getProperties();
        assertThat(props)
                .containsEntry("security.protocol", "SASL_SSL")
                .containsEntry("sasl.mechanism", "SCRAM-SHA-512");
        assertThat(props.get("sasl.jaas.config")).asString().contains("platform-service");
        assertThat(props).containsKey("ssl.truststore.location");
        assertThat(props.get("ssl.truststore.location")).isNotNull();
    }

    @Test
    void blankTrustStoreFailsFast() {
        KafkaProperties kafkaProperties = new KafkaProperties();
        KafkaSecurityProperties security = new KafkaSecurityProperties(
                true, "platform-service", "secret", "  ", "changeit", "PKCS12", "");

        assertThatThrownBy(() -> kafkaSecuritySupport.apply(kafkaProperties, security, RESOURCE_LOADER))
                .isInstanceOf(KafkaSecurityConfigurationException.class)
                .hasMessageContaining("trust-store обязателен");
    }

    @Test
    void missingTrustStoreFailsFast() {
        KafkaProperties kafkaProperties = new KafkaProperties();
        KafkaSecurityProperties security = new KafkaSecurityProperties(
                true,
                "platform-service",
                "secret",
                "classpath:missing-kafka-truststore.p12",
                "changeit",
                "PKCS12",
                ""
        );

        assertThatThrownBy(() -> kafkaSecuritySupport.apply(kafkaProperties, security, RESOURCE_LOADER))
                .isInstanceOf(KafkaSecurityConfigurationException.class)
                .hasMessageContaining("Trust store Kafka не найден");
    }
}
