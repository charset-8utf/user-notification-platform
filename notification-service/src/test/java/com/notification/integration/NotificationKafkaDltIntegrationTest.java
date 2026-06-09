package com.notification.integration;

import com.notification.dto.NotificationEmailRequest;
import com.notification.entity.NotificationDeliveryStatus;
import com.notification.entity.UserNotificationOperation;
import com.notification.repository.NotificationLogRepository;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("kafka")
class NotificationKafkaDltIntegrationTest {

    static final MongoDBContainer MONGO = new MongoDBContainer(DockerImageName.parse("mongo:7"));

    static final ConfluentKafkaContainer KAFKA =
            new ConfluentKafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    static {
        MONGO.start();
        KAFKA.start();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mongodb.uri", () -> MONGO.getConnectionString() + "/notification");
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add("spring.mail.host", () -> "127.0.0.1");
        registry.add("spring.mail.port", () -> "2599");
        registry.add("spring.mail.properties.mail.smtp.auth", () -> "false");
        registry.add("app.notification.site-name", () -> "интеграционный сайт");
        registry.add("app.notification.mail-from", () -> "это-не-email");
        registry.add("app.notification.kafka.retry.max-attempts", () -> "2");
        registry.add("app.notification.kafka.retry.backoff-ms", () -> "200");
    }

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private NotificationLogRepository notificationLogRepository;

    @Value("${app.notification.kafka.topic}")
    private String topic;

    @Value("${app.notification.kafka.dlt-suffix}")
    private String dltSuffix;

    @BeforeEach
    void clean() {
        notificationLogRepository.deleteAll();
    }

    @Test
    void unprocessableMessageGoesToDltAfterMaxAttempts() {
        NotificationEmailRequest event = NotificationEmailRequest.of(
                UserNotificationOperation.USER_CREATED, "victim@example.com");
        kafkaTemplate.send(topic, event.email(), event);

        Map<String, Object> consumerProps = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "dlt-asserter-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class
        );

        try (KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(consumerProps)) {
            consumer.subscribe(List.of(topic + dltSuffix));

            await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
                ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(500));
                assertThat(records.count()).isPositive();
                ConsumerRecord<String, byte[]> first = records.iterator().next();
                assertThat(first.topic()).isEqualTo(topic + dltSuffix);
                assertThat(new String(first.value())).contains("victim@example.com");
            });
        }

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                assertThat(notificationLogRepository.findAll())
                        .hasSize(2)
                        .allMatch(log -> log.getStatus() == NotificationDeliveryStatus.FAILED));
    }
}
