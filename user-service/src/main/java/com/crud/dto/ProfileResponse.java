package com.crud.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProfileResponse(Long id,
                              Long userId,
                              String phone,
                              String address,
                              @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
                              LocalDateTime createdAt,
                              @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
                              LocalDateTime updatedAt) {
}
