package com.platform.bff.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProfileSummary(Long id, String phone, String address) {

    public static ProfileSummary empty() {
        return new ProfileSummary(null, null, null);
    }
}
