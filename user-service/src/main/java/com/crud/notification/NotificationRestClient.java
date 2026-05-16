package com.crud.notification;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * REST-реализация {@link UserNotificationPort}: синхронный POST в notification-service
 * (контракт {@code POST /api/notifications/email}).
 * <p>
 * Активируется профилем {@code rest}, взаимоисключающим с {@code kafka}
 * (если оба активны одновременно — Spring упадёт при старте с {@code NoUniqueBeanDefinitionException},
 * что и является желаемой ранней диагностикой).
 * <p>
 * Защищён Resilience4j circuit breaker {@code notification-service}: при превышении порога
 * ошибок цепь размыкается, дальнейшие вызовы немедленно проваливаются в {@link #publishFallback}.
 */
@Component
@Profile("rest")
@Slf4j
public class NotificationRestClient implements UserNotificationPort {

    public static final String CIRCUIT_BREAKER_NAME = "notification-service";

    private final RestClient notificationRestClient;

    public NotificationRestClient(@Qualifier("notificationServiceRestClient") RestClient notificationRestClient) {
        this.notificationRestClient = notificationRestClient;
    }

    @Override
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "publishFallback")
    public void publish(UserNotificationEvent event) {
        log.debug("Отправляем уведомление в notification-service: {}", event);
        notificationRestClient.post()
                .uri("/api/notifications/email")
                .contentType(MediaType.APPLICATION_JSON)
                .body(event)
                .retrieve()
                .toBodilessEntity();
    }

    /**
     * Fallback: глотает ошибку, чтобы недоступность notification-service не валила бизнес-операцию
     * (пользователь создаётся/удаляется в любом случае; уведомление будет потеряно).
     * Сигнатура соответствует контракту Resilience4j: те же аргументы + {@link Throwable}.
     */
    @SuppressWarnings("unused")
    void publishFallback(UserNotificationEvent event, Throwable ex) {
        log.warn("Fallback: notification-service недоступен (event={}, cause={})", event, ex.toString());
    }
}
