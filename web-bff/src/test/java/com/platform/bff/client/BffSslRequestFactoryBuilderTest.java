package com.platform.bff.client;

import org.junit.jupiter.api.Test;
import org.springframework.http.client.ClientHttpRequestFactory;

import static org.assertj.core.api.Assertions.assertThat;

class BffSslRequestFactoryBuilderTest {

    private final BffSslRequestFactoryBuilder builder = new BffSslRequestFactoryBuilder();

    @Test
    void createSecureFactory_returnsRequestFactory() {
        ClientHttpRequestFactory factory = builder.createSecureFactory();

        assertThat(factory).isNotNull();
    }

    @Test
    void createInsecureDevFactory_returnsRequestFactory() {
        ClientHttpRequestFactory factory = builder.createInsecureDevFactory();

        assertThat(factory).isNotNull();
    }
}
