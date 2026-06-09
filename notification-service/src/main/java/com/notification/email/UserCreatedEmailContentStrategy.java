package com.notification.email;

import com.notification.entity.UserNotificationOperation;
import org.springframework.stereotype.Component;

@Component
class UserCreatedEmailContentStrategy implements EmailContentStrategy {

    @Override
    public UserNotificationOperation operation() {
        return UserNotificationOperation.USER_CREATED;
    }

    @Override
    public String subject() {
        return "Аккаунт создан";
    }

    @Override
    public String body(String siteName) {
        return "Здравствуйте! Ваш аккаунт на сайте " + siteName + " был успешно создан.";
    }
}
