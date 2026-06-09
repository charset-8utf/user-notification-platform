package com.crud.notification.support;

import com.crud.notification.UserNotificationEvent;
import com.crud.notification.UserNotificationPort;

import java.util.Objects;

public abstract class AbstractUserNotificationPublisher implements UserNotificationPort {

    @Override
    public final void publish(UserNotificationEvent event) {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(event.eventId(), "eventId");
        Objects.requireNonNull(event.operation(), "operation");
        Objects.requireNonNull(event.email(), "email");
        doPublish(event);
    }

    protected abstract void doPublish(UserNotificationEvent event);
}
