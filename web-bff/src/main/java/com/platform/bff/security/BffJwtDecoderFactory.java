package com.platform.bff.security;

import com.platform.bff.config.BffJwtProperties;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Component
public class BffJwtDecoderFactory implements JwtDecoderFactory {

    @Override
    public JwtDecoder create(BffJwtProperties properties) {
        String secret = properties.requireSecret();
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException("app.security.jwt.secret must be at least 32 bytes for HS256");
        }
        return NimbusJwtDecoder
                .withSecretKey(new SecretKeySpec(secretBytes, "HmacSHA256"))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
}
