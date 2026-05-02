package com.crud.dto;

import java.time.LocalDateTime;

public record RoleResponse(Long id,
                           String name,
                           LocalDateTime createdAt,
                           LocalDateTime updatedAt) {
}
