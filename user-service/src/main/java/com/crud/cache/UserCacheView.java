package com.crud.cache;

public record UserCacheView(Long id, String email, String status) {
    public static UserCacheView active(Long id, String email) {
        return new UserCacheView(id, email, "ACTIVE");
    }
}
