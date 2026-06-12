package com.notification.service.email;

import com.notification.domain.UserNotificationOperation;

public interface EmailContentStrategy {

    UserNotificationOperation operation();

    String subject();

    String body(String siteName);
}
