package com.crud.service;

import com.crud.dto.RoleRequest;
import com.crud.dto.RoleResponse;
import com.crud.entity.Role;
import com.crud.entity.User;
import com.crud.exception.RoleNotFoundException;
import com.crud.exception.UserNotFoundException;
import com.crud.exception.UserServiceException;
import com.crud.mapper.RoleMapper;
import com.crud.repository.RoleRepository;
import com.crud.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Сервис ролей с валидацией и retry. */
@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final RoleMapper roleMapper;

    @Override
    @Transactional
    public RoleResponse createRole(RoleRequest request) {
        try {
            Role role = roleMapper.toEntity(request);
            Role saved = roleRepository.save(role);
            log.info("Создана роль: id={}, name={}", saved.getId(), saved.getName());
            return roleMapper.toResponse(saved);
        } catch (DataIntegrityViolationException e) {
            throw new UserServiceException("Роль с именем '" + request.name() + "' уже существует");
        }
    }

    @Override
    public RoleResponse findRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException(id));
        return roleMapper.toResponse(role);
    }

    @Override
    public Page<RoleResponse> findAllRoles(Pageable pageable) {
        return roleRepository.findAll(pageable)
                .map(roleMapper::toResponse);
    }

    @Override
    @Transactional
    @Retryable(
            retryFor = {OptimisticLockingFailureException.class},
            backoff = @Backoff(delay = 100)
    )
    public RoleResponse updateRole(Long id, RoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException(id));
        roleMapper.toEntity(request, role);
        Role updated = roleRepository.save(role);
        log.info("Обновлена роль id={}", updated.getId());
        return roleMapper.toResponse(updated);
    }

    @Recover
    public RoleResponse recover(OptimisticLockingFailureException e, Long id, RoleRequest request) {
        log.error("Не удалось обновить роль с id={} после попыток. Request: {}", id, request);
        throw new UserServiceException("Конфликт версии при обновлении роли. Повторите операцию позже.", e);
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new RoleNotFoundException(id);
        }
        roleRepository.deleteById(id);
        log.info("Удалена роль id={}", id);
    }

    @Override
    @Transactional
    public void assignRoleToUser(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id " + userId + " не найден"));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException(roleId));
        user.getRoles().add(role);
        log.info("Роль id={} назначена пользователю id={}", roleId, userId);
    }

    @Override
    @Transactional
    public void removeRoleFromUser(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id " + userId + " не найден"));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException(roleId));
        user.getRoles().remove(role);
        log.info("Роль id={} снята с пользователя id={}", roleId, userId);
    }
}
