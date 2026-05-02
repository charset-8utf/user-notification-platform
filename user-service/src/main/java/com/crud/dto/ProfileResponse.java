package com.crud.dto;

import java.time.LocalDateTime;

public record ProfileResponse(Long id,
                              Long userId,
                              String phone,
                              String address,
                              LocalDateTime createdAt,
                              LocalDateTime updatedAt) {
}
