package com.crud.dto;

import java.time.LocalDateTime;

/**
 * DTO для ответа клиенту при запросе данных пользователя.
 * Содержит полную информацию о пользователе, включая идентификатор и дату создания.
 * <p>
 * Реализован как record, что гарантирует неизменяемость ответа.
 * </p>
 *
 * @param id        уникальный идентификатор пользователя
 * @param name      имя пользователя
 * @param email     электронная почта
 * @param age       возраст
 * @param createdAt дата и время создания записи
 */
public record UserResponse(Long id, String name, String email, Integer age, LocalDateTime createdAt) {}