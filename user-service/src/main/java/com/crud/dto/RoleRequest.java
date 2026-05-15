package com.crud.dto;

import com.crud.config.Sanitized;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record RoleRequest(
        @JsonProperty("name")
        @NotBlank(message = "Название роли не может быть пустым")
        @Sanitized
        String name) {

    @JsonCreator
    public RoleRequest(@JsonProperty("name") String name) {
        this.name = name;
    }
}
