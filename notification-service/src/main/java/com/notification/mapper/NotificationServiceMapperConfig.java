package com.notification.mapper;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

/** Общие настройки MapStruct для notification-service. */
@MapperConfig(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface NotificationServiceMapperConfig {
}
