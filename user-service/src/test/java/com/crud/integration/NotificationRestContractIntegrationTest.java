package com.crud.integration;

import com.crud.notification.UserNotificationEvent;
import com.crud.notification.UserNotificationOperation;
import com.crud.notification.UserNotificationPort;
import com.crud.support.ServiceJwtTestSupport;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles({"test", "rest"})
class NotificationRestContractIntegrationTest {

    private static final WireMockServer WIREMOCK = new WireMockServer(wireMockConfig().dynamicPort());

    @BeforeAll
    static void startWireMock() {
        WIREMOCK.start();
        WIREMOCK.stubFor(post(urlEqualTo("/api/notifications/email"))
                .withHeader("Authorization", matching("Bearer .+"))
                .withRequestBody(equalToJson("""
                        {
                          "operation": "USER_CREATED",
                          "email": "contract@example.com"
                        }
                        """, true, true))
                .willReturn(aResponse().withStatus(204)));
    }

    @AfterAll
    static void stopWireMock() {
        WIREMOCK.stop();
    }

    @DynamicPropertySource
    static void contractStubProperties(DynamicPropertyRegistry registry) {
        registry.add("app.notification.rest.base-url", () -> "http://localhost:" + WIREMOCK.port());
        registry.add("app.notification.rest.insecure-ssl", () -> "true");
        registry.add("app.security.service-jwt.secret", () -> ServiceJwtTestSupport.TEST_SECRET);
    }

    @Autowired
    private UserNotificationPort port;

    @Test
    void publish_shouldSucceedAgainstContractStub() {
        assertThatCode(() -> port.publish(
                UserNotificationEvent.create(UserNotificationOperation.USER_CREATED, "contract@example.com")))
                .doesNotThrowAnyException();
    }
}
