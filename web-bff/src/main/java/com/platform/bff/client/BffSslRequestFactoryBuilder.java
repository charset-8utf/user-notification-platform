package com.platform.bff.client;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.http.HttpClient;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

@Component
public class BffSslRequestFactoryBuilder {

    public ClientHttpRequestFactory createSecureFactory() {
        HttpClient httpClient = HttpClient.newBuilder().build();
        return new JdkClientHttpRequestFactory(httpClient);
    }

    public ClientHttpRequestFactory createInsecureDevFactory() {
        try {
            TrustManager[] trustManagers = new TrustManager[]{new InsecureDevTrustManager()};
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagers, new SecureRandom());
            HttpClient httpClient = HttpClient.newBuilder()
                    .sslContext(sslContext)
                    .build();
            return new JdkClientHttpRequestFactory(httpClient);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to create insecure SSL RestClient factory for BFF", ex);
        }
    }

    @SuppressWarnings("java:S4830")
    private static final class InsecureDevTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
            throw new UnsupportedOperationException(
                    "Mutual TLS is not used for BFF - backend REST clients");
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
            // Dev-only (app.bff.insecure-ssl=true): server certificate validation intentionally disabled.
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}
