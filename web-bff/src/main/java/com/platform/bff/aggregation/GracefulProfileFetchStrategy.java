package com.platform.bff.aggregation;

import com.platform.bff.client.UserServiceClient;
import com.platform.bff.dto.ProfileSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GracefulProfileFetchStrategy implements ProfileFetchStrategy {

    private final UserServiceClient userServiceClient;

    @Override
    public ProfileSummary fetch(Long userId, String authorizationHeader) {
        if (userId == null) {
            return ProfileSummary.empty();
        }
        try {
            ProfileSummary profile = userServiceClient.getProfileByUserId(userId, authorizationHeader);
            return profile != null ? profile : ProfileSummary.empty();
        } catch (Exception ignored) {
            return ProfileSummary.empty();
        }
    }
}
