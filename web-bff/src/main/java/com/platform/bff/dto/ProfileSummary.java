package com.platform.bff.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.Nullable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProfileSummary(@Nullable Long id, @Nullable String phone, @Nullable String address) {

    public static ProfileSummary empty() {
        return new ProfileSummary(null, null, null);
    }
}
