package com.crud.config.rest;

import com.crud.security.servicejwt.ServiceJwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.ClientHttpRequestInterceptor;

@Configuration
@Profile("rest")
public class ServiceJwtRestClientConfig {

    @Bean
    ClientHttpRequestInterceptor serviceJwtRestClientInterceptor(ServiceJwtTokenProvider tokenProvider) {
        return (request, body, execution) -> {
            request.getHeaders().setBearerAuth(tokenProvider.getToken());
            return execution.execute(request, body);
        };
    }
}
