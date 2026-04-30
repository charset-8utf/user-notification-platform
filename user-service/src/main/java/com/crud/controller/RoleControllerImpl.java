package com.crud.controller;

import com.crud.dto.Page;
import com.crud.dto.Pageable;
import com.crud.dto.RoleRequest;
import com.crud.dto.RoleResponse;
import com.crud.service.RoleService;
import lombok.extern.slf4j.Slf4j;

/**
 * Реализация контроллера ролей.
 */
@Slf4j
public class RoleControllerImpl implements RoleController {

    private final RoleService roleService;

    public RoleControllerImpl(RoleService roleService) {
        this.roleService = roleService;
    }

    @Override
    public RoleResponse createRole(RoleRequest request) {
        log.debug("Контроллер: создание роли");
        return roleService.createRole(request);
    }

    @Override
    public RoleResponse findRoleById(Long id) {
        log.debug("Контроллер: поиск роли по ID: {}", id);
        return roleService.findRoleById(id);
    }

    @Override
    public Page<RoleResponse> findAllRoles(Pageable pageable) {
        log.debug("Контроллер: получение ролей с пагинацией: page={}, size={}", pageable.page(), pageable.size());
        return roleService.findAllRoles(pageable);
    }

    @Override
    public RoleResponse updateRole(Long id, RoleRequest request) {
        log.debug("Контроллер: обновление роли ID: {}", id);
        return roleService.updateRole(id, request);
    }

    @Override
    public void deleteRole(Long id) {
        log.debug("Контроллер: удаление роли ID: {}", id);
        roleService.deleteRole(id);
    }

    @Override
    public void assignRoleToUser(Long userId, Long roleId) {
        log.debug("Контроллер: назначение роли {} пользователю {}", roleId, userId);
        roleService.assignRoleToUser(userId, roleId);
    }

    @Override
    public void removeRoleFromUser(Long userId, Long roleId) {
        log.debug("Контроллер: удаление роли {} у пользователя {}", roleId, userId);
        roleService.removeRoleFromUser(userId, roleId);
    }
}
