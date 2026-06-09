package com.crud.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * REST-клиент к notification-service (профиль {@code rest}).
 * {@link LoadBalanced} — разрешение {@code notification-service} через Eureka.
 */
@Configuration
@Profile("rest")
@EnableConfigurationProperties(NotificationRestTlsProperties.class)
@Slf4j
public class NotificationRestClientConfig {

    @Bean
    @LoadBalanced
    RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean(name = "notificationServiceRestClient")
    public RestClient notificationServiceRestClient(
            RestClient.Builder loadBalancedRestClientBuilder,
            NotificationRestSslContextFactory sslContextFactory,
            @Value("${app.notification.rest.base-url}") String baseUrl,
            @Value("${app.notification.rest.insecure-ssl:false}") boolean insecureSsl,
            @Value("${app.notification.rest.connect-timeout:PT2S}") Duration connectTimeout,
            @Value("${app.notification.rest.read-timeout:PT5S}") Duration readTimeout,
            ClientHttpRequestInterceptor serviceJwtRestClientInterceptor,
            ResourceLoader resourceLoader,
            NotificationRestTlsProperties tlsProperties
    ) {
        HttpClient.Builder httpClientBuilder = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(connectTimeout);
        if (insecureSsl) {
            log.warn("notification REST client: insecure-ssl=true — проверка TLS сертификата отключена (только dev)");
            httpClientBuilder.sslContext(sslContextFactory.insecureSslContext());
        } else {
            httpClientBuilder.sslContext(
                    sslContextFactory.sslContextFromTrustStore(resourceLoader, tlsProperties));
        }
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClientBuilder.build());
        factory.setReadTimeout(readTimeout);
        return loadBalancedRestClientBuilder
                .baseUrl(baseUrl)
                .requestInterceptor(serviceJwtRestClientInterceptor)
                .requestFactory(factory)
                .build();
    }
}
