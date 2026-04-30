package com.crud.controller;

import com.crud.dto.Page;
import com.crud.dto.Pageable;
import com.crud.dto.RoleRequest;
import com.crud.dto.RoleResponse;

/**
 * Контроллер ролей.
 */
public interface RoleController {

    /**
     * Создаёт роль.
     */
    RoleResponse createRole(RoleRequest request);

    /**
     * Находит роль по ID.
     */
    RoleResponse findRoleById(Long id);

    /**
     * Возвращает страницу ролей.
     */
    Page<RoleResponse> findAllRoles(Pageable pageable);

    /**
     * Обновляет роль.
     */
    RoleResponse updateRole(Long id, RoleRequest request);

    /**
     * Удаляет роль.
     */
    void deleteRole(Long id);

    /**
     * Назначает роль пользователю.
     */
    void assignRoleToUser(Long userId, Long roleId);

    /**
     * Отзывает роль у пользователя.
     */
    void removeRoleFromUser(Long userId, Long roleId);
}
