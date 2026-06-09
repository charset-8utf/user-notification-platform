package com.platform.gateway;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("cloud-it")
class ApiGatewayCloudComponentIntegrationTest {

    private static final WireMockServer DOWNSTREAM = new WireMockServer(wireMockConfig().dynamicPort());

    @LocalServerPort
    private int gatewayPort;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeAll
    static void startDownstream() {
        DOWNSTREAM.start();
        DOWNSTREAM.stubFor(get(urlEqualTo("/api/users/42"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .withBody("{\"id\":42,\"name\":\"Gateway User\",\"email\":\"gw@example.com\"}")));
    }

    @AfterAll
    static void stopDownstream() {
        DOWNSTREAM.stop();
    }

    @DynamicPropertySource
    static void gatewayRoutes(DynamicPropertyRegistry registry) {
        registry.add("test.downstream.user-service-uri", () -> "http://localhost:" + DOWNSTREAM.port());
    }

    @Test
    void gateway_shouldProxyUsersRouteToDownstream() {
        webTestClient.get()
                .uri("http://localhost:" + gatewayPort + "/api/users/42")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.email").isEqualTo("gw@example.com");

        DOWNSTREAM.verify(getRequestedFor(urlEqualTo("/api/users/42")));
        WireMock.resetAllRequests();
    }
}
