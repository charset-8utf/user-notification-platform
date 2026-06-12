package com.platform.gateway.security;

import com.platform.gateway.config.GatewayJwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class GatewayReactiveJwtDecoderFactory implements ReactiveJwtDecoderFactory {

    private final JwtValidatorComposer jwtValidatorComposer;

    @Override
    public ReactiveJwtDecoder create(GatewayJwtProperties properties) {
        if (properties.oidcEnabled()) {
            return createOidcDecoder(properties);
        }
        return createHs256Decoder(properties);
    }

    private ReactiveJwtDecoder createOidcDecoder(GatewayJwtProperties properties) {
        String issuerUri = properties.requireIssuerUri();
        NimbusReactiveJwtDecoder decoder = properties.hasJwkSetUri()
                ? NimbusReactiveJwtDecoder.withJwkSetUri(properties.requireJwkSetUri()).build()
                : NimbusReactiveJwtDecoder.withIssuerLocation(issuerUri).build();
        decoder.setJwtValidator(jwtValidatorComposer.compose(issuerUri, properties.issuer()));
        return decoder;
    }

    private ReactiveJwtDecoder createHs256Decoder(GatewayJwtProperties properties) {
        String secret = properties.requireSecret();
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException(
                    "app.security.jwt.secret должен быть не короче 32 байт для HS256");
        }
        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder
                .withSecretKey(new SecretKeySpec(secretBytes, "HmacSHA256"))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(jwtValidatorComposer.compose(null, properties.issuer()));
        return decoder;
    }
}
