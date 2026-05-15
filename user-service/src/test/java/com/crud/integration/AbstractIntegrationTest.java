package com.crud.integration;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "it"})
@Execution(ExecutionMode.SAME_THREAD)
public abstract class AbstractIntegrationTest {

    @LocalServerPort
    protected int port;

    protected RestTemplate restTemplate;

    protected String baseUrl() {
        return "http://localhost:" + port + "/api";
    }

    protected HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth("admin", "admin123");
        return headers;
    }

    protected String uniqueEmail(String prefix) {
        String safe = prefix == null ? "user" : prefix.replaceAll("[^a-zA-Z0-9_-]", "");
        if (safe.isEmpty()) {
            safe = "user";
        }
        return safe + "-" + UUID.randomUUID() + "@integration.local";
    }

    protected String usersByEmailUrl(String email) {
        return UriComponentsBuilder.fromUriString(baseUrl() + "/users/by-email")
                .queryParam("email", email)
                .encode(StandardCharsets.UTF_8)
                .toUriString();
    }

    protected String usersSearchUrl(String emailPart) {
        return UriComponentsBuilder.fromUriString(baseUrl() + "/users/search")
                .queryParam("email", emailPart)
                .encode(StandardCharsets.UTF_8)
                .toUriString();
    }

    protected String uniqueRoleName(String prefix) {
        String safe = prefix == null ? "ROLE" : prefix.replaceAll("[^a-zA-Z0-9_-]", "");
        if (safe.isEmpty()) {
            safe = "ROLE";
        }
        return safe + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    @BeforeEach
    void setUpRestTemplate() {
        restTemplate = new RestTemplate();

        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(@NotNull ClientHttpResponse response) {
                return false;
            }
        });
    }
}
