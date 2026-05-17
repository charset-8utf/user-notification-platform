package com.crud.config;

import com.crud.exception.KafkaSecurityConfigurationException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KafkaSecuritySupportTest {

    private static final ResourceLoader RESOURCE_LOADER = new DefaultResourceLoader();

    @Test
    void appliesSaslSslProperties() {
        KafkaProperties kafkaProperties = new KafkaProperties();
        KafkaSecurityProperties security = new KafkaSecurityProperties(
                true,
                "user-service",
                "secret",
                "classpath:kafka-truststore.p12",
                "changeit",
                "PKCS12",
                ""
        );
        KafkaSecuritySupport.apply(kafkaProperties, security, RESOURCE_LOADER);

        var props = kafkaProperties.getProperties();
        assertThat(props)
                .containsEntry("security.protocol", "SASL_SSL")
                .containsEntry("sasl.mechanism", "SCRAM-SHA-512");
        assertThat(props.get("sasl.jaas.config")).asString().contains("user-service");
        assertThat(props).containsKey("ssl.truststore.location");
        assertThat(props.get("ssl.truststore.location")).isNotNull();
    }

    @Test
    void configurationConstructorAppliesSecurity() {
        KafkaProperties kafkaProperties = new KafkaProperties();
        KafkaSecurityProperties security = new KafkaSecurityProperties(
                true,
                "user-service",
                "secret",
                "classpath:kafka-truststore.p12",
                "changeit",
                "PKCS12",
                ""
        );
        new KafkaSecurityConfiguration(kafkaProperties, security, RESOURCE_LOADER);
        assertThat(kafkaProperties.getProperties()).containsEntry("security.protocol", "SASL_SSL");
    }

    @Test
    void blankTrustStoreFailsFast() {
        KafkaProperties kafkaProperties = new KafkaProperties();
        KafkaSecurityProperties security = new KafkaSecurityProperties(
                true, "user-service", "secret", "  ", "changeit", "PKCS12", "");

        assertThatThrownBy(() -> apply(kafkaProperties, security))
                .isInstanceOf(KafkaSecurityConfigurationException.class)
                .hasMessageContaining("trust-store is required");
    }

    @Test
    void missingTrustStoreFailsFast() {
        KafkaProperties kafkaProperties = new KafkaProperties();
        KafkaSecurityProperties security = new KafkaSecurityProperties(
                true,
                "user-service",
                "secret",
                "classpath:missing-kafka-truststore.p12",
                "changeit",
                "PKCS12",
                ""
        );

        assertThatThrownBy(() -> apply(kafkaProperties, security))
                .isInstanceOf(KafkaSecurityConfigurationException.class)
                .hasMessageContaining("Kafka trust store not found");
    }

    private static void apply(KafkaProperties kafkaProperties, KafkaSecurityProperties security) {
        KafkaSecuritySupport.apply(kafkaProperties, security, RESOURCE_LOADER);
    }
}
