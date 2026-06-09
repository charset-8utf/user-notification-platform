package com.platform.gateway.fallback;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayFallbackControllerTest {

    private final GatewayFallbackController controller = new GatewayFallbackController(
            new UserServiceFallbackHandler(),
            new NotificationServiceFallbackHandler());

    @Test
    void userFallback_returnsServiceUnavailable() {
        var response = controller.userServiceFallback();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).containsEntry("service", "user-service");
    }

    @Test
    void notificationFallback_returnsServiceUnavailable() {
        var response = controller.notificationServiceFallback();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).containsEntry("service", "notification-service");
    }
}
