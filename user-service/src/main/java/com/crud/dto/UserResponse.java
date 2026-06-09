package com.crud.dto;

import com.crud.entity.NotificationDeliveryStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse(Long id,
                           String name,
                           String email,
                           Integer age,
                           NotificationDeliveryStatus notificationDeliveryStatus,
                           @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
                           LocalDateTime createdAt) {}
