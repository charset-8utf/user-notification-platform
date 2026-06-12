package com.platform.bff.client;

import com.platform.bff.config.BffClientProperties;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

class BffRestClientFactoryTest {

    @Test
    void create_buildsRestClientWithPlainBuilder() {
        BffRestClientFactory factory = new BffRestClientFactory(
                RestClient.builder(),
                RestClient.builder(),
                new BffSslRequestFactoryBuilder(),
                new BffClientProperties("http://user-service", "http://notification-service", false, false));

        RestClient client = factory.create("http://user-service");

        assertThat(client).isNotNull();
    }

    @Test
    void create_usesInsecureSslWhenConfigured() {
        BffRestClientFactory factory = new BffRestClientFactory(
                RestClient.builder(),
                RestClient.builder(),
                new BffSslRequestFactoryBuilder(),
                new BffClientProperties("http://user-service", "http://notification-service", true, true));

        RestClient client = factory.create("http://notification-service");

        assertThat(client).isNotNull();
    }
}
