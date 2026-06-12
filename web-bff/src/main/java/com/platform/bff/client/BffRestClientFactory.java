package com.platform.bff.client;

import com.platform.bff.config.BffClientProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class BffRestClientFactory {

    @LoadBalanced
    private final RestClient.Builder loadBalancedRestClientBuilder;
    private final RestClient.Builder plainRestClientBuilder;
    private final BffSslRequestFactoryBuilder sslRequestFactoryBuilder;
    private final BffClientProperties clientProperties;

    public RestClient create(String baseUrl) {
        RestClient.Builder builder = (clientProperties.loadBalanced()
                ? loadBalancedRestClientBuilder
                : plainRestClientBuilder)
                .baseUrl(baseUrl);
        return builder
                .requestFactory(clientProperties.insecureSsl()
                        ? sslRequestFactoryBuilder.createInsecureDevFactory()
                        : sslRequestFactoryBuilder.createSecureFactory())
                .build();
    }
}
