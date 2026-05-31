package com.platform.bff.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class BffRestClientFactory {

    private final RestClient.Builder loadBalancedRestClientBuilder;
    private final RestClient.Builder plainRestClientBuilder;
    private final BffSslRequestFactoryBuilder sslRequestFactoryBuilder;
    private final boolean loadBalanced;
    private final boolean insecureSsl;

    public BffRestClientFactory(
            @LoadBalanced RestClient.Builder loadBalancedRestClientBuilder,
            RestClient.Builder plainRestClientBuilder,
            BffSslRequestFactoryBuilder sslRequestFactoryBuilder,
            @Value("${app.bff.load-balanced:true}") boolean loadBalanced,
            @Value("${app.bff.insecure-ssl:false}") boolean insecureSsl) {
        this.loadBalancedRestClientBuilder = loadBalancedRestClientBuilder;
        this.plainRestClientBuilder = plainRestClientBuilder;
        this.sslRequestFactoryBuilder = sslRequestFactoryBuilder;
        this.loadBalanced = loadBalanced;
        this.insecureSsl = insecureSsl;
    }

    public RestClient create(String baseUrl) {
        RestClient.Builder builder = (loadBalanced ? loadBalancedRestClientBuilder : plainRestClientBuilder)
                .baseUrl(baseUrl);
        return builder
                .requestFactory(insecureSsl
                        ? sslRequestFactoryBuilder.createInsecureDevFactory()
                        : sslRequestFactoryBuilder.createSecureFactory())
                .build();
    }
}
