package com.crud.mapper;

import com.crud.dto.RoleRequest;
import com.crud.dto.RoleResponse;
import com.crud.entity.Role;

/**
 * Маппер ролей.
 */
public interface RoleMapper {

    Role toEntity(RoleRequest request);
    RoleResponse toResponse(Role role);
    Role toEntity(RoleRequest request, Role existing);
}
