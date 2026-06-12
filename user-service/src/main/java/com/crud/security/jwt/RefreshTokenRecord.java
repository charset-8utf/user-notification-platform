package com.crud.security.jwt;

import org.jspecify.annotations.Nullable;

import java.util.List;

public record RefreshTokenRecord(String username,
                                 List<String> roles,
                                 @Nullable String email) {

    public RefreshTokenRecord(String username, java.util.List<String> roles) {
        this(username, roles, null);
    }
}
