package com.notification.config;

import com.notification.exception.KafkaSecurityConfigurationException;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class KafkaSecuritySupport {

    private KafkaSecuritySupport() {
    }

    public static void apply(
            KafkaProperties kafkaProperties,
            KafkaSecurityProperties security,
            ResourceLoader resourceLoader
    ) {
        var props = kafkaProperties.getProperties();
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
        props.put(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-512");
        props.put(SaslConfigs.SASL_JAAS_CONFIG, jaasConfig(security.username(), security.password()));
        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG,
                resolveTrustStorePath(resourceLoader, security.trustStore()));
        props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, security.trustStorePassword());
        props.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, security.trustStoreType());
        props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG,
                security.endpointIdentificationAlgorithm());
    }

    private static String jaasConfig(String username, String password) {
        return "org.apache.kafka.common.security.scram.ScramLoginModule required "
                + "username=\"" + username + "\" password=\"" + password + "\";";
    }

    private static String resolveTrustStorePath(ResourceLoader resourceLoader, String location) {
        if (!StringUtils.hasText(location)) {
            throw new KafkaSecurityConfigurationException("app.kafka.security.trust-store is required");
        }
        Resource resource = resourceLoader.getResource(location);
        if (!resource.exists()) {
            throw new KafkaSecurityConfigurationException("Kafka trust store not found: " + location);
        }
        try {
            if (resource.isFile()) {
                return resource.getFile().getAbsolutePath();
            }
            Path temp = Files.createTempFile("kafka-truststore-", ".p12");
            temp.toFile().deleteOnExit();
            try (InputStream in = resource.getInputStream()) {
                Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
            }
            return temp.toAbsolutePath().toString();
        } catch (IOException ex) {
            throw new KafkaSecurityConfigurationException(
                    "Failed to resolve Kafka trust store: " + location, ex);
        }
    }
}
