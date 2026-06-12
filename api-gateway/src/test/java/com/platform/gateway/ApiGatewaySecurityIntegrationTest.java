package com.platform.gateway;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.platform.gateway.support.GatewayJwtTestSupport;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"cloud", "gateway-sec-it"})
class ApiGatewaySecurityIntegrationTest {

    private static final WireMockServer DOWNSTREAM = new WireMockServer(wireMockConfig().dynamicPort());

    @LocalServerPort
    private int gatewayPort;

    private WebTestClient webTestClient;

    @BeforeEach
    void configureWebTestClient() {
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + gatewayPort)
                .build();
    }

    @BeforeAll
    static void startDownstream() {
        DOWNSTREAM.start();
        DOWNSTREAM.stubFor(get(urlEqualTo("/api/users/7"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .withBody("{\"id\":7}")));
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
    void protectedRoute_withoutJwt_returns401() {
        webTestClient.get()
                .uri("/api/users/7")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void protectedRoute_withJwt_proxiesToDownstream() {
        webTestClient.get()
                .uri("http://localhost:" + gatewayPort + "/api/users/7")
                .header(HttpHeaders.AUTHORIZATION, GatewayJwtTestSupport.bearerToken())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(7);
    }
}
