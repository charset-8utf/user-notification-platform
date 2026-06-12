package com.platform.bff.client;

import com.platform.bff.config.BffClientProperties;
import com.platform.bff.dto.ProfileSummary;
import com.platform.bff.dto.UserSummary;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class UserServiceClient {

    private final RestClient restClient;

    @Autowired
    public UserServiceClient(BffRestClientFactory restClientFactory, BffClientProperties clientProperties) {
        this(restClientFactory.create(clientProperties.userServiceBaseUrl()));
    }

    UserServiceClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public @Nullable UserSummary getCurrentUser(String bearerToken) {
        return restClient.get()
                .uri("/api/users/me")
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .retrieve()
                .body(UserSummary.class);
    }

    public @Nullable ProfileSummary getProfileByUserId(long userId, String bearerToken) {
        return restClient.get()
                .uri("/api/profiles/user/{userId}", userId)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .retrieve()
                .body(ProfileSummary.class);
    }
}
