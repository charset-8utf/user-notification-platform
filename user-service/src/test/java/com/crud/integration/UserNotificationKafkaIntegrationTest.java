package com.crud.integration;

import com.crud.dto.UserRequest;
import com.crud.dto.UserResponse;
import com.crud.notification.UserNotificationOperation;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
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
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "it", "kafka", "redis"})
@Testcontainers(disabledWithoutDocker = true)
class UserNotificationKafkaIntegrationTest {

    @Container
    static final ConfluentKafkaContainer KAFKA =
            new ConfluentKafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    @Container
    static final GenericContainer<?> REDIS =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                    .withExposedPorts(6379);

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
        // ускоряем — кэш с дефолтным TTL не критичен в тесте
        registry.add("app.cache.redis.ttl", () -> "PT5M");
        registry.add("app.notification.kafka.outbox.relay-interval-ms", () -> "200");
    }

    @LocalServerPort
    int port;

    @Autowired
    StringRedisTemplate redis;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void createUser_ShouldWriteRedisAndPublishUserCreated() throws Exception {
        String email = "kafka-create-" + UUID.randomUUID() + "@example.com";

        try (KafkaConsumer<String, String> consumer = newConsumer("user-svc-it-create")) {
            consumer.subscribe(List.of("user-notifications"));
            consumer.poll(Duration.ofMillis(200));

            UserResponse created = postUser(new UserRequest("Kafka Create", email, 30));

            JsonNode payload = pollUntil(consumer, UserNotificationOperation.USER_CREATED, email);
            assertThat(payload.get("email").asText()).isEqualTo(email);
            assertThat(payload.get("eventId").asText()).isNotBlank();

            await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
                String cached = redis.opsForValue().get("user:" + created.id());
                assertThat(cached).isNotNull();
                JsonNode view = objectMapper.readTree(cached);
                assertThat(view.get("email").asText()).isEqualTo(email);
                assertThat(view.get("status").asText()).isEqualTo("ACTIVE");
            });
        }
    }

    @Test
    void deleteUser_ShouldEvictRedisAndPublishUserDeletedWithEmail() throws Exception {
        String email = "kafka-delete-" + UUID.randomUUID() + "@example.com";
        UserResponse created = postUser(new UserRequest("Kafka Delete", email, 31));

        try (KafkaConsumer<String, String> consumer = newConsumer("user-svc-it-delete")) {
            consumer.subscribe(List.of("user-notifications"));
            consumer.poll(Duration.ofMillis(200));

            deleteUser(created.id());

            JsonNode payload = pollUntil(consumer, UserNotificationOperation.USER_DELETED, email);
            assertThat(payload.get("email").asText()).isEqualTo(email);

            await().atMost(Duration.ofSeconds(5))
                    .untilAsserted(() -> assertThat(redis.opsForValue().get("user:" + created.id())).isNull());
        }
    }

    private UserResponse postUser(UserRequest request) {
        ResponseEntity<UserResponse> response = restTemplate().exchange(
                baseUrl() + "/users", HttpMethod.POST, new HttpEntity<>(request, authHeaders()), UserResponse.class);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
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
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth("admin", "admin123");
        return headers;
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

    /**
     * Читает топик, пока не найдёт сообщение с заданными {@code operation} и {@code email}
     * (топик может содержать события от других тестов — пропускаем их).
     */
    private JsonNode pollUntil(
            KafkaConsumer<String, String> consumer,
            UserNotificationOperation expectedOperation,
            String expectedEmail
    ) throws Exception {
        long deadline = System.currentTimeMillis() + 10_000;
        while (System.currentTimeMillis() < deadline) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
            for (ConsumerRecord<String, String> record : records) {
                JsonNode payload = objectMapper.readTree(record.value());
                if (expectedOperation.name().equals(payload.get("operation").asText())
                        && expectedEmail.equals(payload.get("email").asText())) {
                    return payload;
                }
            }
        }
        throw new AssertionError("Не дождались события " + expectedOperation + " для " + expectedEmail);
    }
}
