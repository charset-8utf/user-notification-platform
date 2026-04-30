package com.crud.dto;

public record RoleRequest(Long id, String name) {

    public RoleRequest(String name) {
        this(null, name);
    }
}
