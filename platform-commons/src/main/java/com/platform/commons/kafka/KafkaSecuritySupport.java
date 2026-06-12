package com.platform.commons.kafka;

import com.platform.commons.io.SecureTempFiles;
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

/**
 * SASL_SSL + SCRAM-SHA-512 для Kafka-клиентов (Strategy для применения security-свойств).
 */
public class KafkaSecuritySupport {

    public void apply(
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

    private String jaasConfig(String username, String password) {
        return "org.apache.kafka.common.security.scram.ScramLoginModule required "
                + "username=\"" + username + "\" password=\"" + password + "\";";
    }

    private String resolveTrustStorePath(ResourceLoader resourceLoader, String location) {
        if (!StringUtils.hasText(location)) {
            throw new KafkaSecurityConfigurationException("app.kafka.security.trust-store обязателен");
        }
        Resource resource = resourceLoader.getResource(location);
        if (!resource.exists()) {
            throw new KafkaSecurityConfigurationException("Trust store Kafka не найден: " + location);
        }
        if (resource.isFile()) {
            return resolveFileResourcePath(resource, location);
        }
        return copyResourceToTempFile(resource, location);
    }

    private String resolveFileResourcePath(Resource resource, String location) {
        try {
            return resource.getFile().getAbsolutePath();
        } catch (IOException ex) {
            throw new KafkaSecurityConfigurationException(
                    "Не удалось получить файл trust store Kafka: " + location, ex);
        }
    }

    private String copyResourceToTempFile(Resource resource, String location) {
        try {
            Path temp = SecureTempFiles.createTempFile("kafka-truststore-", ".p12");
            temp.toFile().deleteOnExit();
            try (InputStream in = resource.getInputStream()) {
                Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
            }
            return temp.toAbsolutePath().toString();
        } catch (IOException ex) {
            throw new KafkaSecurityConfigurationException(
                    "Не удалось скопировать trust store Kafka во временный файл: " + location, ex);
        }
    }
}
