package com.crud.mapper;

import com.crud.dto.RoleRequest;
import com.crud.dto.RoleResponse;
import com.crud.entity.Role;

import java.util.List;

/**
 * Маппер ролей.
 */
public interface RoleMapper {

    Role toEntity(RoleRequest request);
    RoleResponse toResponse(Role role);
    List<RoleResponse> toResponseList(List<Role> roles);
    Role toEntity(RoleRequest request, Role existing);
}
