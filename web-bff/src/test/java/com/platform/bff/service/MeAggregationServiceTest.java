package com.platform.bff.service;

import com.platform.bff.aggregation.ProfileFetchStrategy;
import com.platform.bff.client.NotificationServiceClient;
import com.platform.bff.client.UserServiceClient;
import com.platform.bff.dto.MeResponse;
import com.platform.bff.dto.NotificationSummary;
import com.platform.bff.dto.ProfileSummary;
import com.platform.bff.dto.UserSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeAggregationServiceTest {

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private NotificationServiceClient notificationServiceClient;

    @Mock
    private ProfileFetchStrategy profileFetchStrategy;

    @InjectMocks
    private MeAggregationService service;

    @Test
    void loadCurrentUser_aggregatesDownstreamData() {
        UserSummary user = new UserSummary(1L, "admin", "admin@example.com", 30);
        ProfileSummary profile = new ProfileSummary(10L, "+7999", "Moscow");
        NotificationSummary notification = new NotificationSummary("email", "DELIVERED", "Welcome");
        when(userServiceClient.getCurrentUser("Bearer token")).thenReturn(user);
        when(profileFetchStrategy.fetch(1L, "Bearer token")).thenReturn(profile);
        when(notificationServiceClient.fetchLatest("admin@example.com", "Bearer token")).thenReturn(notification);

        MeResponse response = service.loadCurrentUser("Bearer token");

        assertThat(response.user()).isEqualTo(user);
        assertThat(response.profile()).isEqualTo(profile);
        assertThat(response.lastNotification()).isEqualTo(notification);
        verify(profileFetchStrategy).fetch(1L, "Bearer token");
        verify(notificationServiceClient).fetchLatest("admin@example.com", "Bearer token");
    }
}
