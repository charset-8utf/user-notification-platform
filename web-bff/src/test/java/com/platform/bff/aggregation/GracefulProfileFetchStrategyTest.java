package com.platform.bff.aggregation;

import com.platform.bff.client.UserServiceClient;
import com.platform.bff.dto.ProfileSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GracefulProfileFetchStrategyTest {

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private GracefulProfileFetchStrategy strategy;

    @Test
    void returnsEmptyWhenUserIdMissing() {
        assertThat(strategy.fetch(null, "Bearer token")).isEqualTo(ProfileSummary.empty());
    }

    @Test
    void returnsProfileWhenDownstreamSucceeds() {
        ProfileSummary profile = new ProfileSummary(10L, "+7999", "Moscow");
        when(userServiceClient.getProfileByUserId(1L, "Bearer token")).thenReturn(profile);

        assertThat(strategy.fetch(1L, "Bearer token")).isEqualTo(profile);
    }

    @Test
    void returnsEmptyWhenDownstreamReturnsNull() {
        when(userServiceClient.getProfileByUserId(1L, "Bearer token")).thenReturn(null);

        assertThat(strategy.fetch(1L, "Bearer token")).isEqualTo(ProfileSummary.empty());
    }

    @Test
    void returnsEmptyWhenDownstreamFails() {
        when(userServiceClient.getProfileByUserId(1L, "Bearer token"))
                .thenThrow(new RestClientException("user-service unavailable"));

        assertThat(strategy.fetch(1L, "Bearer token")).isEqualTo(ProfileSummary.empty());
    }
}
