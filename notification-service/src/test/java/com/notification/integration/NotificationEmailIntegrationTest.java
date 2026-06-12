package com.notification.integration;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;
import com.notification.domain.NotificationDeliveryStatus;
import com.notification.domain.UserNotificationOperation;
import com.notification.repository.NotificationLogRepository;
import com.notification.support.ServiceJwtTestSupport;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("rest")
class NotificationEmailIntegrationTest {

    @Container
    static final MongoDBContainer MONGO = new MongoDBContainer(DockerImageName.parse("mongo:7"));

    static GreenMail greenMail;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        if (greenMail == null) {
            int smtpPort;
            try (ServerSocket socket = new ServerSocket(0)) {
                smtpPort = socket.getLocalPort();
            } catch (Exception e) {
                throw new IllegalStateException("Не удалось зарезервировать SMTP-порт", e);
            }
            greenMail = new GreenMail(new ServerSetup(smtpPort, "127.0.0.1", ServerSetup.PROTOCOL_SMTP));
            greenMail.start();
        }
        registry.add("spring.mongodb.uri", () -> MONGO.getConnectionString() + "/notification");
        registry.add("spring.mail.host", () -> "127.0.0.1");
        registry.add("spring.mail.port", () -> String.valueOf(greenMail.getSmtp().getPort()));
        registry.add("spring.mail.properties.mail.smtp.auth", () -> "false");
        registry.add("app.notification.site-name", () -> "интеграционный сайт");
        registry.add("app.notification.mail-from", () -> "noreply@test.local");
        registry.add("app.security.service-jwt.secret", () -> ServiceJwtTestSupport.TEST_SECRET);
        registry.add("server.ssl.enabled", () -> "false");
    }

    private static final String BEARER = ServiceJwtTestSupport.bearerToken();

    @LocalServerPort
    private int port;

    @Autowired
    private NotificationLogRepository notificationLogRepository;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @BeforeEach
    void clean() {
        notificationLogRepository.deleteAll();
        if (greenMail != null) {
            greenMail.reset();
        }
    }

    @AfterAll
    static void tearDown() {
        if (greenMail != null) {
            greenMail.stop();
            greenMail = null;
        }
    }

    @Test
    void postEmailPersistsLogAndSendsSmtp() throws Exception {
        String body = """
                {
                  "eventId": "660e8400-e29b-41d4-a716-446655440000",
                  "operation": "USER_CREATED",
                  "email": "user@example.com"
                }
                """;
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/notifications/email"))
                .header("Content-Type", "application/json")
                .header("Authorization", BEARER)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());

        assertThat(response.statusCode()).isEqualTo(204);

        assertThat(notificationLogRepository.findAll()).hasSize(1);
        assertThat(notificationLogRepository.findAll().getFirst().getStatus()).isEqualTo(NotificationDeliveryStatus.SENT);
        assertThat(notificationLogRepository.findAll().getFirst().getOperation()).isEqualTo(UserNotificationOperation.USER_CREATED);

        assertThat(greenMail.getReceivedMessages()).hasSize(1);
        MimeMessage received = greenMail.getReceivedMessages()[0];
        assertThat(decodeBody(received)).contains("интеграционный сайт");
    }

    private static String decodeBody(MimeMessage message) {
        String raw = GreenMailUtil.getBody(message);
        try {
            return new String(Base64.getMimeDecoder().decode(raw), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return raw;
        }
    }

    @Test
    void postEmailDeletedAccountUsesDeletionTemplate() throws Exception {
        String body = """
                {
                  "eventId": "660e8400-e29b-41d4-a716-446655440001",
                  "operation": "USER_DELETED",
                  "email": "gone@example.com"
                }
                """;
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/notifications/email"))
                .header("Content-Type", "application/json")
                .header("Authorization", BEARER)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());

        assertThat(response.statusCode()).isEqualTo(204);
        assertThat(greenMail.getReceivedMessages()[0].getSubject()).contains("удал");
    }
}
