package com.platform.bff.security;

import com.platform.bff.config.BffJwtProperties;
import org.springframework.security.oauth2.jwt.JwtDecoder;

/**
 * Factory Method: создание {@link JwtDecoder} для HS256.
 */
public interface JwtDecoderFactory {

    JwtDecoder create(BffJwtProperties properties);
}
