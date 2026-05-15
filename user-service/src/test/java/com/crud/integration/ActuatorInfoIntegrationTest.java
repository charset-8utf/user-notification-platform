package com.crud.integration;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ActuatorInfoIntegrationTest {

    private static final ParameterizedTypeReference<Map<String, Object>> INFO_BODY_TYPE =
            new ParameterizedTypeReference<>() {};

    @LocalServerPort
    private int port;

    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
    }

    @Test
    void actuatorInfo_ShouldExposeApplicationBlockFromConfig() {
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "http://localhost:" + port + "/actuator/info",
                HttpMethod.GET,
                null,
                INFO_BODY_TYPE);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .extractingByKey("application")
                .asInstanceOf(InstanceOfAssertFactories.map(String.class, Object.class))
                .containsEntry("name", "user-service")
                .containsEntry("description", "REST user service (Spring Boot)");
    }
}
