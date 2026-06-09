package com.crud.dto;

import jakarta.validation.constraints.NotBlank;

public record NoteRequest(
        @NotBlank(message = "Текст заметки не может быть пустым")
        String content
) {
}
