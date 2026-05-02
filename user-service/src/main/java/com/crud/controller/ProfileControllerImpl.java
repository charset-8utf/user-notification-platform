package com.crud.controller;

import com.crud.dto.Page;
import com.crud.dto.Pageable;
import com.crud.dto.ProfileRequest;
import com.crud.dto.ProfileResponse;
import com.crud.service.ProfileService;
import lombok.extern.slf4j.Slf4j;

/**
 * Реализация контроллера профилей.
 */
@Slf4j
public class ProfileControllerImpl implements ProfileController {

    private final ProfileService profileService;

    public ProfileControllerImpl(ProfileService profileService) {
        this.profileService = profileService;
    }

    @Override
    public ProfileResponse createProfile(Long userId, ProfileRequest request) {
        log.debug("Контроллер: создание профиля для пользователя ID: {}", userId);
        return profileService.createProfile(userId, request);
    }

    @Override
    public ProfileResponse updateProfile(Long userId, ProfileRequest request) {
        log.debug("Контроллер: обновление профиля пользователя ID: {}", userId);
        return profileService.updateProfile(userId, request);
    }

    @Override
    public ProfileResponse findProfileByUserId(Long userId) {
        log.debug("Контроллер: поиск профиля пользователя ID: {}", userId);
        return profileService.findProfileByUserId(userId);
    }

    @Override
    public Page<ProfileResponse> findAllProfiles(Pageable pageable) {
        log.debug("Контроллер: запрос профилей с пагинацией: page={}, size={}", pageable.page(), pageable.size());
        return profileService.findAllProfiles(pageable);
    }

    @Override
    public void deleteProfile(Long userId) {
        log.debug("Контроллер: удаление профиля пользователя ID: {}", userId);
        profileService.deleteProfile(userId);
    }
}
