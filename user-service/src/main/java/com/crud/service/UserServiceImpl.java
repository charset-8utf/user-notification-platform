package com.crud.service;

import com.crud.dto.UserRequest;
import com.crud.dto.UserResponse;
import com.crud.entity.User;
import com.crud.exception.UserNotFoundException;
import com.crud.exception.ValidationException;
import com.crud.mapper.UserMapper;
import com.crud.repository.UserRepository;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Реализация {@link UserService} с валидацией и использованием репозитория.
 * <p>
 * <ul>
 *     <li>имя не может быть пустым</li>
 *     <li>email должен соответствовать формату</li>
 *     <li>возраст в диапазоне 0–150</li>
 * </ul>
 * </p>
 */
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;

    /**
     * Конструктор с внедрением зависимости репозитория.
     *
     * @param userRepository репозиторий для доступа к базе данных
     */
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Выполняет валидацию полей запроса.
     *
     * @param request DTO с данными
     * @throws ValidationException если какое-либо поле некорректно
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public UserResponse createUser(UserRequest request) {
        validate(request);
        User user = UserMapper.toEntity(request);
        User saved = userRepository.save(user);
        log.info("Создан пользователь: id={}, email={}", saved.getId(), saved.getEmail());
        return UserMapper.toResponse(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserResponse getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserMapper::toResponse)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserResponse> getAllUsers() {
        return UserMapper.toResponseList(userRepository.findAll());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserResponse updateUser(Long id, UserRequest request) {
        validate(request);
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        User updated = UserMapper.toEntity(request, existing);
        User saved = userRepository.update(updated);
        log.info("Обновлён пользователь: id={}, email={}", saved.getId(), saved.getEmail());
        return UserMapper.toResponse(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteUser(Long id) {
        userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        userRepository.deleteById(id);
        log.info("Удалён пользователь: id={}", id);
    }
}