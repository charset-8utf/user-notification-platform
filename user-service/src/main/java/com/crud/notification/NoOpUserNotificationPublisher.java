package com.crud.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Активируется, когда ни {@code kafka}, ни {@code rest}-профиль не включён:
 * локальная отладка без брокера/notification-service не должна падать.
 */
@Component
@Profile("!kafka & !rest")
@Slf4j
public class NoOpUserNotificationPublisher implements UserNotificationPort {

    @Override
    public void publish(UserNotificationEvent event) {
        log.debug("Нотификация подавлена (no-op publisher): {}", event);
    }
}
