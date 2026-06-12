package com.crud.exception;

import com.crud.security.ApiOutputSanitizer;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExceptionMessageResolver {

    private final ApiOutputSanitizer apiOutputSanitizer;

    public String resolve(@Nullable String message, String defaultMessage) {
        String source = message != null ? message : defaultMessage;
        return apiOutputSanitizer.sanitizeRequired(source);
    }
}
