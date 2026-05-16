package com.notification.mapper;

import com.notification.dto.NotificationEmailRequest;
import com.notification.entity.NotificationChannel;
import com.notification.entity.NotificationDeliveryStatus;
import com.notification.entity.NotificationLog;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/** Реализация маппера документов истории уведомлений. */
@Component
public class NotificationLogMapperImpl implements NotificationLogMapper {

    @Override
    public NotificationLog toEntity(NotificationEmailRequest request,
                                    NotificationDeliveryStatus status,
                                    String errorMessage) {
        return NotificationLog.builder()
                .id(UUID.randomUUID().toString())
                .operation(request.operation())
                .email(request.email())
                .channel(NotificationChannel.EMAIL)
                .status(status)
                .errorMessage(errorMessage)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
