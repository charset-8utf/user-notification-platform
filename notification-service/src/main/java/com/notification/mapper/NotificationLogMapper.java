package com.notification.mapper;

import com.notification.domain.NotificationDeliveryStatus;
import com.notification.dto.NotificationEmailRequest;
import com.notification.dto.NotificationLogSummaryResponse;
import com.notification.entity.NotificationLog;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Маппер между DTO, доменом и Mongo-документами истории уведомлений (MapStruct). */
@Mapper(config = NotificationServiceMapperConfig.class, uses = NotificationLogDetailResolver.class)
public interface NotificationLogMapper {

    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID().toString())")
    @Mapping(target = "channel", expression = "java(com.notification.domain.NotificationChannel.EMAIL)")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "operation", source = "request.operation")
    @Mapping(target = "email", source = "request.email")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "errorMessage", source = "errorMessage")
    NotificationLog toEntity(NotificationEmailRequest request,
                             NotificationDeliveryStatus status,
                             @Nullable String errorMessage);

    @Mapping(target = "found", constant = "true")
    @Mapping(target = "operation", expression = "java(log.getOperation().name())")
    @Mapping(target = "channel", expression = "java(log.getChannel().name())")
    @Mapping(target = "status", expression = "java(log.getStatus().name())")
    @Mapping(target = "detail", source = "errorMessage", qualifiedByName = "resolveDetail")
    NotificationLogSummaryResponse toSummary(NotificationLog log);
}
