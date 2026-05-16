package com.crud.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Конфигурация HTTP-клиента к notification-service для профиля {@code rest}.
 * Использует JDK {@link HttpClient}: явный connect-timeout, read-timeout — на самой фабрике.
 */
@Configuration
@Profile("rest")
public class NotificationRestClientConfig {

    @Bean(name = "notificationServiceRestClient")
    public RestClient notificationServiceRestClient(
            @Value("${app.notification.rest.base-url}") String baseUrl,
            @Value("${app.notification.rest.connect-timeout:PT2S}") Duration connectTimeout,
            @Value("${app.notification.rest.read-timeout:PT5S}") Duration readTimeout
    ) {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(connectTimeout)
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(readTimeout);
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }
}
