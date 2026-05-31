package com.crud.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import org.springframework.core.io.ClassPathResource;

import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.security.KeyStore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationRestSslContextFactoryTest {

    private final NotificationRestSslContextFactory factory = new NotificationRestSslContextFactory();

    @Test
    void loadsClasspathTrustStore() throws Exception {
        NotificationRestTlsProperties tls = new NotificationRestTlsProperties(
                "classpath:notification-truststore.p12",
                "changeit",
                "PKCS12"
        );
        SSLContext context = factory.sslContextFromTrustStore(
                new DefaultResourceLoader(), tls);
        assertThat(context).isNotNull();
        assertThat(context.getProtocol()).isEqualTo("TLS");

        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        try (InputStream in = new ClassPathResource("notification-truststore.p12").getInputStream()) {
            trustStore.load(in, "changeit".toCharArray());
        }
        assertThat(trustStore.containsAlias("platform-dev-ca")).isTrue();
    }

    @Test
    void insecureSslContextForDevOnly() {
        assertThat(factory.insecureSslContext()).isNotNull();
    }

    @Test
    void missingTrustStoreFailsFast() {
        NotificationRestTlsProperties tls = new NotificationRestTlsProperties(
                "classpath:missing-truststore.p12",
                "changeit",
                "PKCS12"
        );
        DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
        assertThatThrownBy(() -> factory.sslContextFromTrustStore(resourceLoader, tls))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Trust store not found");
    }
}
