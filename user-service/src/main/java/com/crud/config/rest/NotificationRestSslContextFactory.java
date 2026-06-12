package com.crud.config.rest;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

/**
 * TLS для REST-клиента notification-service.
 */
@Component
public class NotificationRestSslContextFactory {

    public SSLContext sslContextFromTrustStore(
            ResourceLoader resourceLoader,
            NotificationRestTlsProperties tls
    ) {
        if (!StringUtils.hasText(tls.trustStore())) {
            throw new IllegalStateException(
                    "При insecure-ssl=false требуется app.notification.rest.tls.trust-store");
        }
        try {
            Resource resource = resourceLoader.getResource(tls.trustStore());
            if (!resource.exists()) {
                throw new IllegalStateException("Trust store не найден: " + tls.trustStore());
            }
            KeyStore trustStore = KeyStore.getInstance(tls.trustStoreType());
            char[] password = tls.trustStorePassword().toCharArray();
            try (InputStream in = resource.getInputStream()) {
                trustStore.load(in, password);
            }
            TrustManagerFactory trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
            return context;
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Не удалось загрузить trust store notification-service", ex);
        }
    }

    public SSLContext insecureSslContext() {
        try {
            TrustManager[] trustManagers = new TrustManager[]{new InsecureDevTrustManager()};
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, trustManagers, new SecureRandom());
            return context;
        } catch (Exception ex) {
            throw new IllegalStateException("Не удалось настроить небезопасный SSL для REST-клиента notification-service", ex);
        }
    }

    @SuppressWarnings("java:S4830")
    private static final class InsecureDevTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
            throw new UnsupportedOperationException(
                    "Взаимная TLS (mTLS) не используется для REST-клиента notification-service");
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
            // Проверка сертификата сервера намеренно отключена.
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}
