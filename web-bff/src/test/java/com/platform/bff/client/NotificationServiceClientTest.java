package com.platform.bff.client;

import com.platform.bff.dto.NotificationSummary;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class NotificationServiceClientTest {

    private final RestClient.Builder builder = RestClient.builder();
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    private final NotificationServiceClient client = new NotificationServiceClient(builder.build());

    @AfterEach
    void verifyServer() {
        server.verify();
    }

    @Test
    void fetchLatest_returnsSummaryWhenFound() {
        server.expect(requestTo("/api/notifications/logs/latest?email=admin@example.com"))
                .andExpect(header("Authorization", "Bearer token"))
                .andRespond(withSuccess("""
                        {
                          "found": true,
                          "channel": "email",
                          "status": "DELIVERED",
                          "detail": "Welcome"
                        }
                        """, MediaType.APPLICATION_JSON));

        NotificationSummary summary = client.fetchLatest("admin@example.com", "Bearer token");

        assertThat(summary.status()).isEqualTo("DELIVERED");
        assertThat(summary.detail()).isEqualTo("Welcome");
    }

    @Test
    void fetchLatest_returnsUnavailableWhenResponseBodyMissing() {
        server.expect(requestTo("/api/notifications/logs/latest?email=admin@example.com"))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

        NotificationSummary summary = client.fetchLatest("admin@example.com", "Bearer token");

        assertThat(summary.status()).isEqualTo("UNAVAILABLE");
    }

    @Test
    void fetchLatest_returnsUnavailableWhenDownstreamFails() {
        server.expect(requestTo("/api/notifications/logs/latest?email=admin@example.com"))
                .andRespond(withServerError());

        NotificationSummary summary = client.fetchLatest("admin@example.com", "Bearer token");

        assertThat(summary.status()).isEqualTo("UNAVAILABLE");
        assertThat(summary.detail()).contains("notification-service недоступен");
    }
}
