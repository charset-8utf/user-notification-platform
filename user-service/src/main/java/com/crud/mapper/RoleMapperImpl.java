package com.crud.mapper;

import com.crud.dto.RoleRequest;
import com.crud.dto.RoleResponse;
import com.crud.entity.Role;

import java.util.List;

/**
 * Реализация маппера ролей.
 */
public class RoleMapperImpl implements RoleMapper {

    @Override
    public Role toEntity(RoleRequest request) {
        return Role.builder()
                .id(request.id())
                .name(request.name())
                .build();
    }

    @Override
    public RoleResponse toResponse(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }

    @Override
    public List<RoleResponse> toResponseList(List<Role> roles) {
        if (roles == null) {
            return List.of();
        }
        return roles.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public Role toEntity(RoleRequest request, Role existing) {
        existing.setName(request.name());
        return existing;
    }
}
