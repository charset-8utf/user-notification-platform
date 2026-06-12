package com.platform.bff.client;

import com.platform.bff.config.BffClientProperties;
import com.platform.bff.dto.NotificationLogResponse;
import com.platform.bff.dto.NotificationSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class NotificationServiceClient {

    private final RestClient restClient;

    @Autowired
    public NotificationServiceClient(BffRestClientFactory restClientFactory, BffClientProperties clientProperties) {
        this(restClientFactory.create(clientProperties.notificationServiceBaseUrl()));
    }

    NotificationServiceClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public NotificationSummary fetchLatest(String email, String bearerToken) {
        try {
            NotificationLogResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/notifications/logs/latest")
                            .queryParam("email", email)
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .retrieve()
                    .body(NotificationLogResponse.class);
            if (response == null) {
                return unavailable(email);
            }
            return response.toSummary();
        } catch (RestClientException ex) {
            return unavailable(email);
        }
    }

    private NotificationSummary unavailable(String email) {
        return new NotificationSummary("EMAIL", "UNAVAILABLE",
                "notification-service недоступен для " + email);
    }
}
