package com.notification.mapper;

import org.mapstruct.Named;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

@Component
public class NotificationLogDetailResolver {

    private static final String SUCCESS_DETAIL = "OK";

    @Named("resolveDetail")
    public String resolve(@Nullable String errorMessage) {
        return errorMessage != null ? errorMessage : SUCCESS_DETAIL;
    }
}
