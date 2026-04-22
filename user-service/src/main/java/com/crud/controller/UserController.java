package com.crud.controller;

import com.crud.dto.UserRequest;
import com.crud.dto.UserResponse;
import com.crud.exception.UserNotFoundException;
import com.crud.exception.ValidationException;

import java.util.List;

/**
 * Контроллер для обработки запросов, связанных с пользователями.
 * <p>
 * Является входной точкой в приложение со стороны пользовательского интерфейса (консоли).
 * Делегирует выполнение бизнес-логики сервису {@link com.crud.service.UserService}.
 * </p>
 * <p>
 * В текущей реализации все методы просто вызывают соответствующие методы сервиса.
 * </p>
 */
public interface UserController {

    /**
     * Создаёт нового пользователя.
     *
     * @param request DTO с данными пользователя (имя, email, возраст)
     * @return DTO созданного пользователя (с id и датой создания)
     * @throws ValidationException если данные не прошли валидацию
     * @throws com.crud.exception.DataAccessException если произошла ошибка БД (например, дубликат email)
     */
    UserResponse createUser(UserRequest request);

    /**
     * Возвращает пользователя по идентификатору.
     *
     * @param id идентификатор пользователя
     * @return DTO пользователя
     * @throws UserNotFoundException если пользователь не найден
     */
    UserResponse findUserById(Long id);

    /**
     * Возвращает список всех пользователей.
     *
     * @return список DTO всех пользователей (может быть пустым)
     */
    List<UserResponse> findAllUsers();

    /**
     * Обновляет данные существующего пользователя.
     *
     * @param id      идентификатор обновляемого пользователя
     * @param request DTO с новыми данными
     * @return DTO обновлённого пользователя
     * @throws UserNotFoundException если пользователь не найден
     * @throws ValidationException   если новые данные некорректны
     */
    UserResponse updateUser(Long id, UserRequest request);

    /**
     * Удаляет пользователя по идентификатору.
     *
     * @param id идентификатор удаляемого пользователя
     * @throws UserNotFoundException если пользователь не найден
     */
    void deleteUser(Long id);
}