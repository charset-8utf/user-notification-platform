package com.crud.service;

import com.crud.dto.Page;
import com.crud.dto.Pageable;
import com.crud.dto.RoleRequest;
import com.crud.dto.RoleResponse;

/**
 * Сервис ролей.
 */
public interface RoleService {

    RoleResponse createRole(RoleRequest request);
    RoleResponse findRoleById(Long id);
    Page<RoleResponse> findAllRoles(Pageable pageable);
    RoleResponse updateRole(Long id, RoleRequest request);
    void deleteRole(Long id);
    void assignRoleToUser(Long userId, Long roleId);
    void removeRoleFromUser(Long userId, Long roleId);
}
