package com.platform.bff.service;

import com.platform.bff.aggregation.ProfileFetchStrategy;
import com.platform.bff.client.NotificationServiceClient;
import com.platform.bff.client.UserServiceClient;
import com.platform.bff.dto.MeResponse;
import com.platform.bff.dto.NotificationSummary;
import com.platform.bff.dto.ProfileSummary;
import com.platform.bff.dto.UserSummary;
import com.platform.bff.facade.MeFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MeAggregationService implements MeFacade {

    private final UserServiceClient userServiceClient;
    private final NotificationServiceClient notificationServiceClient;
    private final ProfileFetchStrategy profileFetchStrategy;

    @Override
    public MeResponse loadCurrentUser(String authorizationHeader) {
        UserSummary user = userServiceClient.getCurrentUser(authorizationHeader);
        ProfileSummary profile = profileFetchStrategy.fetch(user.id(), authorizationHeader);
        NotificationSummary notification = notificationServiceClient.fetchLatest(user.email(), authorizationHeader);
        return new MeResponse(user, profile, notification);
    }
}
