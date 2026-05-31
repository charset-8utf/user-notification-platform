package com.platform.bff.client;

import com.platform.bff.dto.ProfileSummary;
import com.platform.bff.dto.UserSummary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class UserServiceClient {

    private final RestClient restClient;

    public UserServiceClient(
            BffRestClientFactory restClientFactory,
            @Value("${app.bff.user-service-base-url:https://user-service}") String baseUrl) {
        this.restClient = restClientFactory.create(baseUrl);
    }

    public UserSummary getCurrentUser(String bearerToken) {
        return restClient.get()
                .uri("/api/users/me")
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .retrieve()
                .body(UserSummary.class);
    }

    public ProfileSummary getProfileByUserId(long userId, String bearerToken) {
        return restClient.get()
                .uri("/api/profiles/user/{userId}", userId)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .retrieve()
                .body(ProfileSummary.class);
    }
}
