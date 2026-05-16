package com.crud.service;

import com.crud.cache.UserCachePort;
import com.crud.cache.UserCacheView;
import com.crud.dto.UserRequest;
import com.crud.dto.UserResponse;
import com.crud.entity.User;
import com.crud.exception.UserNotFoundException;
import com.crud.exception.UserServiceException;
import com.crud.exception.ValidationException;
import com.crud.mapper.UserMapper;
import com.crud.notification.UserNotificationEvent;
import com.crud.notification.UserNotificationOperation;
import com.crud.notification.UserNotificationPort;
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

/**
 * Реализация сервиса пользователей.
 */
@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserCachePort userCache;
    private final UserNotificationPort notificationPort;

    @Override
    @Transactional
    public UserResponse createUser(UserRequest request) {
        try {
            User user = userMapper.toEntity(request);
            User saved = userRepository.save(user);
            log.info("Создан пользователь: id={}, email={}", saved.getId(), saved.getEmail());
            userCache.put(UserCacheView.active(saved.getId(), saved.getEmail()));
            notificationPort.publish(
                    UserNotificationEvent.create(UserNotificationOperation.USER_CREATED, saved.getEmail()));
            return userMapper.toResponse(saved);
        } catch (DataIntegrityViolationException e) {
            throw new ValidationException("Email уже используется: " + request.email());
        }
    }

    @Override
    public UserResponse findUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    public Page<UserResponse> findAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toResponse);
    }

    @Override
    @Transactional
    @Retryable(
            retryFor = {OptimisticLockingFailureException.class},
            backoff = @Backoff(delay = 100)
    )
    public UserResponse updateUser(Long id, UserRequest request) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        User updated = userMapper.toEntity(request, existing);
        User saved = userRepository.save(updated);
        log.info("Обновлён пользователь: id={}, email={}", saved.getId(), saved.getEmail());
        userCache.put(UserCacheView.active(saved.getId(), saved.getEmail()));
        return userMapper.toResponse(saved);
    }

    @Recover
    public UserResponse recover(OptimisticLockingFailureException e, Long id, UserRequest request) {
        log.error("Не удалось обновить пользователя с id={} после попыток. Request: {}", id, request);
        throw new UserServiceException("Конфликт версии при обновлении пользователя. Повторите операцию позже.", e);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        // email читаем ДО deleteById, чтобы успеть положить его в событие USER_DELETED
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        String email = existing.getEmail();
        userRepository.deleteById(id);
        log.info("Удалён пользователь: id={}, email={}", id, email);
        userCache.evict(id);
        notificationPort.publish(UserNotificationEvent.create(UserNotificationOperation.USER_DELETED, email));
    }

    @Override
    public UserResponse findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с email " + email + " не найден"));
    }

    @Override
    public Page<UserResponse> searchUsersByEmail(String emailPart, Pageable pageable) {
        return userRepository.findByEmailContaining(emailPart, pageable)
                .map(userMapper::toResponse);
    }
}
