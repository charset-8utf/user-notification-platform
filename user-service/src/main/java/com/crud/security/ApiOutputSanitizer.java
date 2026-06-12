package com.crud.security;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.util.Objects;

@Component
public class ApiOutputSanitizer {

    @Nullable
    public String sanitize(@Nullable String value) {
        if (value == null) {
            return null;
        }
        return sanitizeRequired(value);
    }

    public String sanitizeRequired(String value) {
        return Objects.requireNonNull(HtmlUtils.htmlEscape(value));
    }
}
