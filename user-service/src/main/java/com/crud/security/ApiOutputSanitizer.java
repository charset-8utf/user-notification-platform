package com.crud.security;

import org.springframework.lang.Nullable;
import org.springframework.web.util.HtmlUtils;

public final class ApiOutputSanitizer {

    private ApiOutputSanitizer() {
    }

    @Nullable
    public static String sanitize(@Nullable String value) {
        if (value == null) {
            return null;
        }
        return HtmlUtils.htmlEscape(value);
    }
}
