package com.crud.notification;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Profile("rest")
@Slf4j
public class NotificationRestClient implements UserNotificationPort {

    public static final String RESILIENCE_NAME = "notification-service";

    private final RestClient restClient;

    public NotificationRestClient(@Qualifier("notificationServiceRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    @Bulkhead(name = RESILIENCE_NAME, type = Bulkhead.Type.SEMAPHORE)
    @CircuitBreaker(name = RESILIENCE_NAME, fallbackMethod = "publishFallback")
    public void publish(UserNotificationEvent event) {
        log.debug("Отправляем уведомление в notification-service: {}", event);
        restClient.post()
                .uri("/api/notifications/email")
                .contentType(MediaType.APPLICATION_JSON)
                .body(event)
                .retrieve()
                .toBodilessEntity();
    }

    @SuppressWarnings("unused")
    void publishFallback(UserNotificationEvent event, Throwable ex) {
        log.warn("Fallback: notification-service недоступен (event={}, cause={})", event, ex.toString());
    }
}
