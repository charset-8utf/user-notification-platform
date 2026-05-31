package com.platform.bff.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserSummary(Long id, String name, String email, Integer age) {
}
