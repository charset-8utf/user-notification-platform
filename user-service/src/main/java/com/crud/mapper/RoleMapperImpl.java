package com.crud.mapper;

import com.crud.dto.RoleRequest;
import com.crud.dto.RoleResponse;
import com.crud.entity.Role;
import org.springframework.stereotype.Component;

/**
 * Реализация маппера ролей.
 */
@Component
public class RoleMapperImpl implements RoleMapper {

    @Override
    public Role toEntity(RoleRequest request) {
        return Role.builder()
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
    public Role toEntity(RoleRequest request, Role existing) {
        existing.setName(request.name());
        return existing;
    }
}
