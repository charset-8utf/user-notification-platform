package com.crud.integration;

import com.crud.notification.UserNotificationEvent;
import com.crud.notification.UserNotificationOperation;
import com.crud.notification.UserNotificationPort;
import com.crud.support.ServiceJwtTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Consumer-driven contract: stubs из notification-service (Spring Cloud Contract).
 * Перед запуском: {@code mvn -f notification-service/pom.xml clean install -DskipTests}
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles({"test", "rest"})
@AutoConfigureStubRunner(
        ids = "com.notification:notification-service:0.0.1-SNAPSHOT:stubs:8093",
        stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
class NotificationRestContractIntegrationTest {

    private static final int STUB_PORT = 8093;

    @DynamicPropertySource
    static void contractStubProperties(DynamicPropertyRegistry registry) {
        registry.add("app.notification.rest.base-url", () -> "http://localhost:" + STUB_PORT);
        registry.add("app.notification.rest.insecure-ssl", () -> "true");
        registry.add("app.security.service-jwt.secret", () -> ServiceJwtTestSupport.TEST_SECRET);
        registry.add("spring.cloud.discovery.enabled", () -> "false");
        registry.add("spring.cloud.loadbalancer.enabled", () -> "false");
        registry.add("eureka.client.enabled", () -> "false");
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
