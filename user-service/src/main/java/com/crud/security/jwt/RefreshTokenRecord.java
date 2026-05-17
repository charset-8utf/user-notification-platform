package com.crud.security.jwt;

import java.util.List;

public record RefreshTokenRecord(String username,
                                 List<String> roles) {
}
