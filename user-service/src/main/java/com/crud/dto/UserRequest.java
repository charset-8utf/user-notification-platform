package com.crud.dto;

import com.crud.config.Sanitized;
import jakarta.validation.constraints.*;

public record UserRequest(
        @NotBlank(message = "Имя не может быть пустым")
        @Sanitized
        String name,

        @NotBlank(message = "Email не может быть пустым")
        @Email(message = "Некорректный формат email")
        String email,

        @NotNull(message = "Возраст не может быть null")
        @Min(value = 0, message = "Возраст должен быть не менее 0")
        @Max(value = 150, message = "Возраст должен быть не более 150")
        Integer age
) {
}
