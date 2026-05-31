package com.crud.security;

import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Component
public class JwtSecretKeyFactory {

    public byte[] secretBytes(String secret, String propertyName) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException(propertyName + " must be at least 32 bytes for HS256");
        }
        return bytes;
    }

    public SecretKey secretKey(String secret, String propertyName) {
        return new SecretKeySpec(secretBytes(secret, propertyName), "HmacSHA256");
    }
}
