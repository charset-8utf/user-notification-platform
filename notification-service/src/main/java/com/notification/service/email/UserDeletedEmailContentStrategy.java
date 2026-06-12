package com.notification.service.email;

import com.notification.domain.UserNotificationOperation;
import org.springframework.stereotype.Component;

@Component
class UserDeletedEmailContentStrategy implements EmailContentStrategy {

    @Override
    public UserNotificationOperation operation() {
        return UserNotificationOperation.USER_DELETED;
    }

    @Override
    public String subject() {
        return "Аккаунт удалён";
    }

    @Override
    public String body(String siteName) {
        return "Здравствуйте! Ваш аккаунт был удалён.";
    }
}
