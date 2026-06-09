package com.notification.service;

import com.notification.dto.NotificationEmailRequest;

public interface NotificationService {
    void sendEmailNotification(NotificationEmailRequest request);
}
