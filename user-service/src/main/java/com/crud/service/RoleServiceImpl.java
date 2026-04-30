package com.crud.service;

import com.crud.dto.Page;
import com.crud.dto.Pageable;
import com.crud.dto.RoleRequest;
import com.crud.dto.RoleResponse;
import com.crud.entity.Role;
import com.crud.exception.RoleNotFoundException;
import com.crud.exception.UserNotFoundException;
import com.crud.exception.ValidationException;
import com.crud.mapper.RoleMapper;
import com.crud.mapper.RoleMapperImpl;
import com.crud.repository.RoleRepository;
import com.crud.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

/** Сервис ролей с валидацией и retry. */
@Slf4j
public class RoleServiceImpl extends AbstractService implements RoleService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final RoleMapper roleMapper;

    public RoleServiceImpl(RoleRepository roleRepository, UserRepository userRepository) {
        this(roleRepository, userRepository, new RoleMapperImpl());
    }

    public RoleServiceImpl(RoleRepository roleRepository, UserRepository userRepository, RoleMapper roleMapper) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.roleMapper = roleMapper;
    }

    private void validate(RoleRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new ValidationException("Название роли не может быть пустым");
        }
    }

    @Override
    public RoleResponse createRole(RoleRequest request) {
        validate(request);
        Role role = roleMapper.toEntity(request);
        Role saved = roleRepository.save(role);
        log.info("Создана роль: id={}, name={}", saved.getId(), saved.getName());
        return roleMapper.toResponse(saved);
    }

    @Override
    public RoleResponse findRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException(id));
        return roleMapper.toResponse(role);
    }

    @Override
    public Page<RoleResponse> findAllRoles(Pageable pageable) {
        Page<Role> rolePage = roleRepository.findAll(pageable);
        List<RoleResponse> content = rolePage.content().stream()
                .map(roleMapper::toResponse)
                .toList();
        return new Page<>(content, rolePage.totalElements(), rolePage.page(), rolePage.size());
    }

    @Override
    public RoleResponse updateRole(Long id, RoleRequest request) {
        validate(request);
        return executeWithRetry(() -> {
            Role role = roleRepository.findById(id)
                    .orElseThrow(() -> new RoleNotFoundException(id));
            roleMapper.toEntity(request, role);
            Role updated = roleRepository.update(role);
            log.info("Обновлена роль id={}", updated.getId());
            return roleMapper.toResponse(updated);
        });
    }

    @Override
    public void deleteRole(Long id) {
        roleRepository.deleteById(id);
        log.info("Удалена роль id={}", id);
    }

    @Override
    public void assignRoleToUser(Long userId, Long roleId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new UserNotFoundException("Пользователь с id " + userId + " не найден");
        }
        if (roleRepository.findById(roleId).isEmpty()) {
            throw new RoleNotFoundException(roleId);
        }
        roleRepository.assignRoleToUser(userId, roleId);
        log.info("Роль id={} назначена пользователю id={}", roleId, userId);
    }

    @Override
    public void removeRoleFromUser(Long userId, Long roleId) {
        roleRepository.removeRoleFromUser(userId, roleId);
        log.info("Роль id={} снята с пользователя id={}", roleId, userId);
    }
}
