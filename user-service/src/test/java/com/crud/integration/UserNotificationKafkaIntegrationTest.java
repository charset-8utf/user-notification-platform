package com.crud.integration;

import com.crud.dto.UserRequest;
import com.crud.dto.UserResponse;
import com.crud.entity.NotificationDeliveryStatus;
import com.crud.entity.User;
import com.crud.notification.UserNotificationOperation;
import com.crud.notification.compensation.NotificationCompensationEvent;
import com.crud.repository.UserRepository;
import com.crud.support.JwtAuthTestSupport;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "it", "kafka", "redis", "jwt"})
@Testcontainers(disabledWithoutDocker = true)
class UserNotificationKafkaIntegrationTest {

    @Container
    static final ConfluentKafkaContainer KAFKA =
            new ConfluentKafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    static final GenericContainer<?> REDIS = startRedisContainer();

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
        registry.add("app.cache.redis.ttl", () -> "PT5M");
        registry.add("app.notification.kafka.outbox.relay-interval-ms", () -> "200");
    }

    @AfterAll
    static void stopRedisContainer() {
        REDIS.close();
    }

    @LocalServerPort
    int port;

    @Autowired
    StringRedisTemplate redis;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    UserRepository userRepository;

    @Test
    void compensationEventMarksUserAsFailed() {
        String email = "comp-" + UUID.randomUUID() + "@example.com";
        userRepository.save(User.builder().name("Comp User").email(email).age(30).build());

        NotificationCompensationEvent event = new NotificationCompensationEvent(
                UUID.randomUUID(),
                UserNotificationOperation.USER_CREATED,
                email,
                "smtp timeout",
                Instant.now());
        kafkaTemplate.send("notification-compensations", event.email(), event);

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() ->
                assertThat(userRepository.findByEmail(email))
                        .isPresent()
                        .get()
                        .extracting(User::getNotificationDeliveryStatus)
                        .isEqualTo(NotificationDeliveryStatus.FAILED));
    }

    @Test
    void createUser_ShouldWriteRedisAndPublishUserCreated() {
        String email = "kafka-create-" + UUID.randomUUID() + "@example.com";

        try (KafkaConsumer<String, String> consumer = newConsumer("user-svc-it-create")) {
            consumer.subscribe(List.of("user-notifications"));
            consumer.poll(Duration.ofMillis(200));

            UserResponse created = postUser(new UserRequest("Kafka Create", email, 30));

            JsonNode payload = pollUntil(consumer, UserNotificationOperation.USER_CREATED, email);
            assertThat(jsonField(payload, "email")).isEqualTo(email);
            assertThat(jsonField(payload, "eventId")).isNotBlank();

            await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
                String cached = redis.opsForValue().get("user:" + created.id());
                assertThat(cached).isNotNull();
                JsonNode view = objectMapper.readTree(cached);
                assertThat(jsonField(view, "email")).isEqualTo(email);
                assertThat(jsonField(view, "status")).isEqualTo("ACTIVE");
            });
        }
    }

    @Test
    void deleteUser_ShouldEvictRedisAndPublishUserDeletedWithEmail() {
        String email = "kafka-delete-" + UUID.randomUUID() + "@example.com";
        UserResponse created = postUser(new UserRequest("Kafka Delete", email, 31));

        try (KafkaConsumer<String, String> consumer = newConsumer("user-svc-it-delete")) {
            consumer.subscribe(List.of("user-notifications"));
            consumer.poll(Duration.ofMillis(200));

            deleteUser(created.id());

            JsonNode payload = pollUntil(consumer, UserNotificationOperation.USER_DELETED, email);
            assertThat(jsonField(payload, "email")).isEqualTo(email);

            await().atMost(Duration.ofSeconds(5))
                    .untilAsserted(() -> assertThat(redis.opsForValue().get("user:" + created.id())).isNull());
        }
    }

    @SuppressWarnings("resource")
    private static GenericContainer<?> startRedisContainer() {
        GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                .withExposedPorts(6379);
        container.start();
        return container;
    }

    private UserResponse postUser(UserRequest request) {
        ResponseEntity<UserResponse> response = restTemplate().exchange(
                baseUrl() + "/users", HttpMethod.POST, new HttpEntity<>(request, authHeaders()), UserResponse.class);
        UserResponse body = response.getBody();
        assertThat(body).isNotNull();
        return body;
    }

    private void deleteUser(Long id) {
        restTemplate().exchange(
                baseUrl() + "/users/" + id, HttpMethod.DELETE, new HttpEntity<>(authHeaders()), Void.class);
    }

    private RestTemplate restTemplate() {
        return new RestTemplate();
    }

    private String baseUrl() {
        return "http://localhost:" + port + "/api";
    }

    private HttpHeaders authHeaders() {
        RestTemplate client = restTemplate();
        String token = JwtAuthTestSupport.obtainAccessToken(client, port, "admin", "admin123");
        return JwtAuthTestSupport.bearerHeaders(token);
    }

    private KafkaConsumer<String, String> newConsumer(String groupId) {
        Properties props = new Properties();
        props.putAll(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, groupId,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName(),
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName()
        ));
        return new KafkaConsumer<>(props);
    }

    private JsonNode pollUntil(
            KafkaConsumer<String, String> consumer,
            UserNotificationOperation expectedOperation,
            String expectedEmail
    ) {
        long deadline = System.currentTimeMillis() + 10_000;
        while (System.currentTimeMillis() < deadline) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
            for (ConsumerRecord<String, String> kafkaRecord : records) {
                JsonNode payload = objectMapper.readTree(kafkaRecord.value());
                if (expectedOperation.name().equals(jsonField(payload, "operation"))
                        && expectedEmail.equals(jsonField(payload, "email"))) {
                    return payload;
                }
            }
        }
        throw new AssertionError("Не дождались события " + expectedOperation + " для " + expectedEmail);
    }

    private static String jsonField(JsonNode node, String field) {
        return node.get(field).asString();
    }
}
