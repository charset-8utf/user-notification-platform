package com.notification;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;
import com.notification.dto.NotificationEmailRequest;
import com.notification.entity.NotificationDeliveryStatus;
import com.notification.entity.UserNotificationOperation;
import com.notification.idempotency.ProcessedNotificationEventRepository;
import com.notification.repository.NotificationLogRepository;
import jakarta.mail.internet.MimeMessage;
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
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("kafka")
class NotificationKafkaIntegrationTest {

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
    void kafkaEventPersistsLogAndSendsSmtp() {
        NotificationEmailRequest event = NotificationEmailRequest.of(
                UserNotificationOperation.USER_CREATED, "kafka@example.com");

        kafkaTemplate.send(topic, event.email(), event);

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            var logs = notificationLogRepository.findAll();
            assertThat(logs).hasSize(1);
            assertThat(logs.getFirst().getStatus()).isEqualTo(NotificationDeliveryStatus.SENT);
            assertThat(logs.getFirst().getOperation()).isEqualTo(UserNotificationOperation.USER_CREATED);
            assertThat(logs.getFirst().getEmail()).isEqualTo("kafka@example.com");

            assertThat(greenMail.getReceivedMessages()).hasSize(1);
            MimeMessage received = greenMail.getReceivedMessages()[0];
            assertThat(received.getSubject()).contains("создан");
            assertThat(decodeBody(received)).contains("интеграционный сайт");
        });
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
    void kafkaDeletedEventUsesDeletionTemplate() {
        NotificationEmailRequest event = NotificationEmailRequest.of(
                UserNotificationOperation.USER_DELETED, "gone@example.com");

        kafkaTemplate.send(topic, event.email(), event);

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            assertThat(greenMail.getReceivedMessages()).hasSize(1);
            assertThat(greenMail.getReceivedMessages()[0].getSubject()).contains("удал");
            assertThat(notificationLogRepository.findAll()).hasSize(1);
        });
    }
}
