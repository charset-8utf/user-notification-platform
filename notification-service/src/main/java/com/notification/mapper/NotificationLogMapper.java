package com.notification.mapper;

import com.notification.dto.NotificationEmailRequest;
import com.notification.entity.NotificationDeliveryStatus;
import com.notification.entity.NotificationLog;

/** Маппер документов истории уведомлений. */
public interface NotificationLogMapper {

    NotificationLog toEntity(NotificationEmailRequest request,
                             NotificationDeliveryStatus status,
                             String errorMessage);
}
