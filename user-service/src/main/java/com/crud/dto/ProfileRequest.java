package com.crud.dto;

import com.crud.config.Sanitized;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProfileRequest(
        @NotNull(message = "Номер телефона не может быть null")
        @Size(max = 20, message = "Номер телефона не может быть длиннее 20 символов")
        @Sanitized
        String phone,

        @NotNull(message = "Адрес не может быть null")
        @Size(max = 255, message = "Адрес не может быть длиннее 255 символов")
        @Sanitized
        String address
) {
}
