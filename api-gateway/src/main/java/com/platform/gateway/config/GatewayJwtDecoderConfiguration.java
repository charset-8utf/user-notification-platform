package com.platform.gateway.config;

import com.platform.gateway.security.ReactiveJwtDecoderFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

@Configuration
@Profile("cloud & !cloud-it")
@RequiredArgsConstructor
public class GatewayJwtDecoderConfiguration {

    private final ReactiveJwtDecoderFactory jwtDecoderFactory;

    @Bean
    ReactiveJwtDecoder reactiveJwtDecoder(GatewayJwtProperties properties) {
        return jwtDecoderFactory.create(properties);
    }
}
