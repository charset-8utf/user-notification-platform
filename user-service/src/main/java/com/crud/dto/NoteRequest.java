package com.crud.dto;

import com.crud.config.Sanitized;
import jakarta.validation.constraints.NotBlank;

public record NoteRequest(
        @NotBlank(message = "Текст заметки не может быть пустым")
        @Sanitized
        String content
) {
}
