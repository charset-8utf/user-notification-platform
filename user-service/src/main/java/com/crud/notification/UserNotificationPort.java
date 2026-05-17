package com.crud.notification;


public interface UserNotificationPort {
    void publish(UserNotificationEvent event);
}
