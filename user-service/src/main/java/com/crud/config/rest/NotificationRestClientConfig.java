package com.crud.config.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

/**
 * REST-клиент к notification-service (профиль {@code rest}).
 * <p>
 * Прямой {@link RestClient} по {@code app.notification.rest.base-url} (Docker/K8s DNS + явный порт).
 */
@Configuration
@Profile("rest")
@Slf4j
public class NotificationRestClientConfig {

    @Bean(name = "notificationServiceRestClient")
    public RestClient notificationServiceRestClient(
            NotificationRestSslContextFactory sslContextFactory,
            NotificationRestProperties restProperties,
            ClientHttpRequestInterceptor serviceJwtRestClientInterceptor,
            ResourceLoader resourceLoader,
            NotificationRestTlsProperties tlsProperties
    ) {
        HttpClient.Builder httpClientBuilder = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(restProperties.connectTimeout());
        if (restProperties.insecureSsl()) {
            log.warn("notification REST client: insecure-ssl=true — проверка TLS сертификата отключена (только dev)");
            httpClientBuilder.sslContext(sslContextFactory.insecureSslContext());
        } else {
            httpClientBuilder.sslContext(
                    sslContextFactory.sslContextFromTrustStore(resourceLoader, tlsProperties));
        }
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClientBuilder.build());
        factory.setReadTimeout(restProperties.readTimeout());
        return RestClient.builder()
                .baseUrl(restProperties.baseUrl())
                .requestInterceptor(serviceJwtRestClientInterceptor)
                .requestFactory(factory)
                .build();
    }
}
