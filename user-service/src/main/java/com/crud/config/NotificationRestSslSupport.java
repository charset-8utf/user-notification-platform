package com.crud.config;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
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
 * TLS для RestClient → notification-service.
 */
public final class NotificationRestSslSupport {

    private NotificationRestSslSupport() {
    }

    public static SSLContext sslContextFromTrustStore(
            ResourceLoader resourceLoader,
            NotificationRestTlsProperties tls
    ) {
        if (!StringUtils.hasText(tls.trustStore())) {
            throw new IllegalStateException(
                    "app.notification.rest.tls.trust-store is required when insecure-ssl=false");
        }
        try {
            Resource resource = resourceLoader.getResource(tls.trustStore());
            if (!resource.exists()) {
                throw new IllegalStateException("Trust store not found: " + tls.trustStore());
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
            throw new IllegalStateException("Failed to load notification-service trust store", ex);
        }
    }

    public static SSLContext insecureSslContext() {
        try {
            TrustManager[] trustManagers = new TrustManager[]{new InsecureDevTrustManager()};
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, trustManagers, new SecureRandom());
            return context;
        } catch (Exception ex) {
            throw new IllegalStateException("Не удалось настроить insecure SSL для REST-клиента notification-service", ex);
        }
    }

    private static final class InsecureDevTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
            throw new UnsupportedOperationException(
                    "Mutual TLS is not used for notification-service REST client");
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}
