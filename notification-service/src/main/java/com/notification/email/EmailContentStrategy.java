package com.notification.email;

import com.notification.entity.UserNotificationOperation;

public interface EmailContentStrategy {

    UserNotificationOperation operation();

    String subject();

    String body(String siteName);
}
