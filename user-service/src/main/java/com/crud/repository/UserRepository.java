package com.crud.repository;

import com.crud.entity.User;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью {@link User}.
 * <p>
 * Определяет специфические методы CRUD, необходимые для пользователей.
 * </p>
 */
public interface UserRepository {

    /**
     * Сохраняет нового пользователя в базе данных.
     *
     * @param user сущность User (без id)
     * @return сохранённая сущность с заполненным id
     */
    User save(User user);

    /**
     * Находит пользователя по идентификатору.
     *
     * @param id идентификатор
     * @return Optional с пользователем или пустой Optional
     */
    Optional<User> findById(Long id);

    /**
     * Возвращает всех пользователей.
     *
     * @return список всех пользователей
     */
    List<User> findAll();

    /**
     * Обновляет существующего пользователя.
     *
     * @param user сущность с изменёнными полями (должен содержать id)
     * @return обновлённая сущность
     */
    User update(User user);

    /**
     * Удаляет пользователя по идентификатору.
     *
     * @param id идентификатор удаляемого пользователя
     */
    void deleteById(Long id);
}