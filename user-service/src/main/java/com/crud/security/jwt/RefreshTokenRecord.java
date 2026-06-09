package com.crud.security.jwt;

import java.util.List;

public record RefreshTokenRecord(String username,
                                 List<String> roles,
                                 String email) {

    public RefreshTokenRecord(String username, java.util.List<String> roles) {
        this(username, roles, null);
    }
}
