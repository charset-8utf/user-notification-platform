package com.crud.integration;

import com.crud.notification.NotificationRestClient;
import com.crud.notification.UserNotificationEvent;
import com.crud.notification.UserNotificationOperation;
import com.crud.notification.UserNotificationPort;
import com.crud.support.ServiceJwtTestSupport;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles({"test", "rest"})
class NotificationRestResilienceIntegrationTest {

    private static final WireMockServer WIREMOCK = new WireMockServer(wireMockConfig().dynamicPort());

    @BeforeAll
    static void startWireMock() {
        WIREMOCK.start();
    }

    @AfterAll
    static void stopWireMock() {
        WIREMOCK.stop();
    }

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("app.notification.rest.base-url", () -> "http://localhost:" + WIREMOCK.port());
        registry.add("app.security.service-jwt.secret", () -> ServiceJwtTestSupport.TEST_SECRET);
        registry.add("app.notification.rest.connect-timeout", () -> "PT1S");
        registry.add("app.notification.rest.read-timeout", () -> "PT1S");
    }

    @Autowired
    UserNotificationPort port;

    @Autowired
    CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void reset() {
        WIREMOCK.resetAll();
        circuitBreakerRegistry.circuitBreaker(NotificationRestClient.CIRCUIT_BREAKER_NAME).reset();
    }

    @Test
    void publish_ShouldPostToNotificationService_WithExpectedJson() {
        WIREMOCK.stubFor(post(urlEqualTo("/api/notifications/email"))
                .willReturn(aResponse().withStatus(204)));

        port.publish(UserNotificationEvent.create(UserNotificationOperation.USER_CREATED, "ok@example.com"));

        WIREMOCK.verify(postRequestedFor(urlEqualTo("/api/notifications/email"))
                .withHeader("Authorization", WireMock.matching("Bearer eyJ[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+"))
                .withRequestBody(matchingJsonPath("$.operation", WireMock.equalTo("USER_CREATED")))
                .withRequestBody(matchingJsonPath("$.email", WireMock.equalTo("ok@example.com")))
                .withRequestBody(matchingJsonPath("$.eventId")));
    }

    @Test
    void publish_ShouldFallbackSilently_AndNotOpenCircuit_On401Unauthorized() {
        WIREMOCK.stubFor(post(urlEqualTo("/api/notifications/email"))
                .willReturn(aResponse().withStatus(401)));

        CircuitBreaker breaker = circuitBreakerRegistry.circuitBreaker(NotificationRestClient.CIRCUIT_BREAKER_NAME);
        for (int i = 0; i < 8; i++) {
            port.publish(UserNotificationEvent.create(UserNotificationOperation.USER_CREATED, "auth-" + i + "@example.com"));
        }
        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    void publish_ShouldFallbackSilently_AndOpenCircuit_AfterRepeatedServerErrors() {
        WIREMOCK.stubFor(post(urlEqualTo("/api/notifications/email"))
                .willReturn(aResponse().withStatus(500)));

        CircuitBreaker breaker = circuitBreakerRegistry.circuitBreaker(NotificationRestClient.CIRCUIT_BREAKER_NAME);
        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);

        // sliding-window=10, minimum-number-of-calls=5, failure-rate-threshold=50%
        // → 5 пятисоток подряд должны размыкнуть цепь. Fallback гарантирует отсутствие исключений.
        for (int i = 0; i < 6; i++) {
            port.publish(UserNotificationEvent.create(UserNotificationOperation.USER_CREATED, "fail-" + i + "@example.com"));
        }

        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        // Когда цепь открыта — fallback срабатывает мгновенно, в WireMock новых запросов не приходит.
        int callsBefore = WIREMOCK.findAll(postRequestedFor(urlEqualTo("/api/notifications/email"))).size();
        port.publish(UserNotificationEvent.create(UserNotificationOperation.USER_DELETED, "open@example.com"));
        int callsAfter = WIREMOCK.findAll(postRequestedFor(urlEqualTo("/api/notifications/email"))).size();
        assertThat(callsAfter).isEqualTo(callsBefore);
    }
}
