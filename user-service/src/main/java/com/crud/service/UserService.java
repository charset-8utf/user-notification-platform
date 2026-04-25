package com.crud.service;

import com.crud.dto.UserRequest;
import com.crud.dto.UserResponse;
import com.crud.exception.UserNotFoundException;
import com.crud.exception.ValidationException;

import java.util.List;

/**
 * Сервис для управления пользователями.
 * <p>
 * Содержит бизнес-логику приложения: валидацию входных данных,
 * преобразование DTO в сущности, вызов репозитория и формирование ответов.
 * </p>
 * <p>
 * Все методы работают с DTO ({@link UserRequest}, {@link UserResponse}),
 * скрывая детали работы с базой данных и сущностью {@link com.crud.entity.User}.
 * </p>
 */
public interface UserService {

    /**
     * Создаёт нового пользователя.
     * <p>
     * Выполняет валидацию полей, преобразует запрос в сущность,
     * сохраняет в базу данных и возвращает ответ.
     * </p>
     *
     * @param request DTO с данными для создания (имя, email, возраст)
     * @return DTO с полными данными созданного пользователя (включая id и дату создания)
     * @throws ValidationException если данные не прошли валидацию
     * @throws com.crud.exception.DataAccessException если произошла ошибка при сохранении (например, дубликат email)
     */
    UserResponse createUser(UserRequest request);

    /**
     * Находит пользователя по идентификатору.
     *
     * @param id уникальный идентификатор пользователя
     * @return DTO с данными пользователя
     * @throws UserNotFoundException если пользователь с указанным id не найден
     */
    UserResponse getUserById(Long id);

    /**
     * Возвращает список всех пользователей.
     *
     * @return список DTO всех пользователей (может быть пустым, но не null)
     */
    List<UserResponse> getAllUsers();

    /**
     * Обновляет данные существующего пользователя.
     * <p>
     * Загружает пользователя по id, обновляет его поля из запроса,
     * выполняет валидацию, сохраняет изменения и возвращает обновлённые данные.
     * </p>
     *
     * @param id      идентификатор обновляемого пользователя
     * @param request DTO с новыми значениями полей
     * @return DTO с обновлёнными данными
     * @throws UserNotFoundException если пользователь не найден
     * @throws ValidationException   если новые данные не прошли валидацию
     */
    UserResponse updateUser(Long id, UserRequest request);

    /**
     * Удаляет пользователя по идентификатору.
     * <p>
     * Если пользователь не найден, выбрасывает исключение.
     * </p>
     *
     * @param id идентификатор удаляемого пользователя
     * @throws UserNotFoundException если пользователь не найден
     */
    void deleteUser(Long id);

    /**
     * Находит пользователя по его электронной почте.
     * <p>
     * Использует репозиторий {@link com.crud.repository.UserRepository#findByEmail(String)} для выполнения поиска.
     * Если пользователь с указанным email не найден, выбрасывается {@link UserNotFoundException}.
     * </p>
     *
     * @param email адрес электронной почты (уникальный, не должен быть {@code null} или пустым)
     * @return DTO {@link UserResponse} с полными данными найденного пользователя
     * @throws UserNotFoundException если пользователь с таким email не существует
     */
    UserResponse getUserByEmail(String email);
}