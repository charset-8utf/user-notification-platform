package com.crud.repository;

import com.crud.dto.Page;
import com.crud.dto.Pageable;
import com.crud.entity.Role;
import java.util.Optional;

/**
 * Репозиторий ролей.
 */
public interface RoleRepository {

    Role save(Role role);
    Optional<Role> findById(Long id);
    Page<Role> findAll(Pageable pageable);
    Role update(Role role);
    void deleteById(Long id);
    void assignRoleToUser(Long userId, Long roleId);
    void removeRoleFromUser(Long userId, Long roleId);
}
