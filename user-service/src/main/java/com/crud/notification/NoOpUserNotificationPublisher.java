package com.crud.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!kafka & !rest")
@Slf4j
public class NoOpUserNotificationPublisher extends com.crud.notification.support.AbstractUserNotificationPublisher {

    @Override
    protected void doPublish(UserNotificationEvent event) {
        log.debug("Нотификация подавлена (no-op publisher): {}", event);
    }
}
