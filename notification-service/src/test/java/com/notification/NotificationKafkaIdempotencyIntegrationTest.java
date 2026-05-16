package com.notification;

import com.notification.dto.NotificationEmailRequest;
import com.notification.entity.UserNotificationOperation;
import com.notification.idempotency.ProcessedNotificationEventRepository;
import com.notification.repository.NotificationLogRepository;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.net.ServerSocket;
import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Повторная доставка того же {@code eventId} не должна отправлять второе письмо (идемпотентный consumer).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("kafka")
class NotificationKafkaIdempotencyIntegrationTest {

    static final MongoDBContainer MONGO = new MongoDBContainer(DockerImageName.parse("mongo:7"));

    static final ConfluentKafkaContainer KAFKA =
            new ConfluentKafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    static GreenMail greenMail;

    static {
        MONGO.start();
        KAFKA.start();
    }

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
        registry.add("spring.mongodb.uri",
                () -> "mongodb://" + MONGO.getHost() + ":" + MONGO.getMappedPort(27017) + "/notification");
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add("spring.mail.host", () -> "127.0.0.1");
        registry.add("spring.mail.port", () -> String.valueOf(greenMail.getSmtp().getPort()));
        registry.add("spring.mail.properties.mail.smtp.auth", () -> "false");
        registry.add("app.notification.site-name", () -> "интеграционный сайт");
        registry.add("app.notification.mail-from", () -> "noreply@test.local");
    }

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private NotificationLogRepository notificationLogRepository;

    @Autowired
    private ProcessedNotificationEventRepository processedEventRepository;

    @Value("${app.notification.kafka.topic}")
    private String topic;

    @BeforeEach
    void clean() {
        notificationLogRepository.deleteAll();
        processedEventRepository.deleteAll();
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
        KAFKA.stop();
        MONGO.stop();
    }

    @Test
    void duplicateEventIdIsProcessedOnce() {
        UUID eventId = UUID.randomUUID();
        NotificationEmailRequest event = new NotificationEmailRequest(
                eventId, UserNotificationOperation.USER_CREATED, "dup@example.com");

        kafkaTemplate.send(topic, event.email(), event);
        kafkaTemplate.send(topic, event.email(), event);

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            assertThat(notificationLogRepository.findAll()).hasSize(1);
            assertThat(greenMail.getReceivedMessages()).hasSize(1);
            assertThat(processedEventRepository.findAll()).hasSize(1);
        });
    }
}
