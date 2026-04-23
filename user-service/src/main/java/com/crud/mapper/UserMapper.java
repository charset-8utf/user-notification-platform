package com.crud.mapper;

import com.crud.dto.UserRequest;
import com.crud.dto.UserResponse;
import com.crud.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.time.format.DateTimeFormatter;

/**
 * Утилитарный класс для преобразования между DTO и сущностью User.
 * <p>
 * Реализует паттерн "Маппер" (Mapper).
 * Все методы статические, создание экземпляров запрещено.
 * </p>
 *
 * @author charset-8utf
 * @version 1.0
 */
public final class UserMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private UserMapper() {
        throw new UnsupportedOperationException("Утилитарный класс, создание экземпляров запрещено");
    }

    /**
     * Преобразует {@link UserRequest} в новую сущность {@link User}.
     * <p>
     * Использует {@link User.Builder} для создания объекта.
     * Поля {@code id} и {@code createdAt} не заполняются – они будут установлены
     * при сохранении в базу данных.
     * </p>
     *
     * @param request DTO с данными для создания пользователя
     * @return новая сущность User (без id и без даты создания)
     * @throws IllegalArgumentException если request содержит некорректные данные
     */
    public static User toEntity(UserRequest request) {
        return User.builder()
                .name(request.name())
                .email(request.email())
                .age(request.age())
                .build();
    }

    /**
     * Обновляет существующую сущность {@link User} данными из {@link UserRequest}.
     * <p>
     * Изменяет поля {@code name}, {@code email} и {@code age} у переданного объекта.
     * Поля {@code id} и {@code createdAt} остаются неизменными.
     * </p>
     *
     * @param request  DTO с новыми данными
     * @param existing существующая сущность (должна быть загружена из БД)
     * @return та же самая сущность (для удобства цепочек вызовов)
     */
    public static User toEntity(UserRequest request, User existing) {
        existing.setName(request.name());
        existing.setEmail(request.email());
        existing.setAge(request.age());
        return existing;
    }

    /**
     * Преобразует сущность {@link User} в {@link UserResponse}.
     * <p>
     * Копирует все поля: id, имя, email, возраст, дату создания.
     * </p>
     *
     * @param user сущность из базы данных (не null)
     * @return DTO с полными данными пользователя
     */
    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAge(),
                user.getCreatedAt()
        );
    }

    /**
     * Преобразует список сущностей {@link User} в список {@link UserResponse}.
     * <p>
     * Использует потоковый API для преобразования каждого элемента.
     * Если входной список пуст или равен null, возвращается пустой список.
     * </p>
     *
     * @param users список сущностей (может быть null)
     * @return список DTO (никогда не null)
     */
    public static List<UserResponse> toResponseList(List<User> users) {
        if (users == null) {
            return List.of();
        }
        return users.stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    /**
     * Форматирует LocalDateTime в вид "дд.ММ.гггг ЧЧ:мм:сс".
     *
     * @param dateTime дата-время
     * @return отформатированная строка, или пустая строка если dateTime == null
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DATE_FORMATTER);
    }
}