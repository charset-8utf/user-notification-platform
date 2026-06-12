package com.platform.gateway.security;

import com.platform.gateway.config.GatewayJwtProperties;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

public interface ReactiveJwtDecoderFactory {

    ReactiveJwtDecoder create(GatewayJwtProperties properties);
}
