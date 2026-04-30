package com.crud.controller;

import com.crud.dto.Page;
import com.crud.dto.Pageable;
import com.crud.dto.ProfileRequest;
import com.crud.dto.ProfileResponse;

/**
 * Контроллер профилей.
 */
public interface ProfileController {

    /**
     * Создаёт профиль.
     */
    ProfileResponse createProfile(Long userId, ProfileRequest request);

    /**
     * Обновляет профиль.
     */
    ProfileResponse updateProfile(Long userId, ProfileRequest request);

    /**
     * Находит профиль по ID пользователя.
     */
    ProfileResponse findProfileByUserId(Long userId);

    /**
     * Возвращает страницу профилей.
     */
    Page<ProfileResponse> findAllProfiles(Pageable pageable);

    /**
     * Удаляет профиль.
     */
    void deleteProfile(Long userId);
}
