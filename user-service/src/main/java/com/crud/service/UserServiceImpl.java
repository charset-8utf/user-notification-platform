package com.crud.service;

import com.crud.dto.Page;
import com.crud.dto.Pageable;
import com.crud.dto.UserRequest;
import com.crud.dto.UserResponse;
import com.crud.entity.User;
import com.crud.exception.UserNotFoundException;
import com.crud.exception.ValidationException;
import com.crud.mapper.UserMapper;
import com.crud.mapper.UserMapperImpl;
import com.crud.repository.UserRepository;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Реализация сервиса пользователей.
 */
@Slf4j
public class UserServiceImpl extends AbstractService implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository) {
        this(userRepository, new UserMapperImpl());
    }

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    private void validate(UserRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new ValidationException("Имя не может быть пустым");
        }
        try {
            InternetAddress emailAddr = new InternetAddress(request.email());
            emailAddr.validate();
        } catch (AddressException e) {
            throw new ValidationException("Некорректный формат email");
        }
        if (request.age() == null || request.age() < 0 || request.age() > 150) {
            throw new ValidationException("Возраст должен быть от 0 до 150");
        }
    }

    @Override
    public UserResponse createUser(UserRequest request) {
        validate(request);
        User user = userMapper.toEntity(request);
        User saved = userRepository.save(user);
        log.info("Создан пользователь: id={}, email={}", saved.getId(), saved.getEmail());
        return userMapper.toResponse(saved);
    }

    @Override
    public UserResponse findUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    public Page<UserResponse> findAllUsers(Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);
        List<UserResponse> content = userMapper.toResponseList(userPage.content());
        return new Page<>(content, userPage.totalElements(), userPage.page(), userPage.size());
    }

    @Override
    public UserResponse updateUser(Long id, UserRequest request) {
        validate(request);
        return executeWithRetry(() -> {
            User existing = userRepository.findById(id)
                    .orElseThrow(() -> new UserNotFoundException(id));
            User updated = userMapper.toEntity(request, existing);
            User saved = userRepository.update(updated);
            log.info("Обновлён пользователь: id={}, email={}", saved.getId(), saved.getEmail());
            return userMapper.toResponse(saved);
        });
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        userRepository.deleteById(id);
        log.info("Удалён пользователь: id={}", id);
    }

    @Override
    public UserResponse findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с email " + email + " не найден"));
    }
}
