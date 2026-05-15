package com.crud.service;

import com.crud.dto.ProfileRequest;
import com.crud.dto.ProfileResponse;
import com.crud.entity.Profile;
import com.crud.entity.User;
import com.crud.exception.ProfileNotFoundException;
import com.crud.exception.UserNotFoundException;
import com.crud.exception.UserServiceException;
import com.crud.mapper.ProfileMapper;
import com.crud.repository.ProfileRepository;
import com.crud.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Сервис профилей с валидацией и retry. */
@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final ProfileMapper profileMapper;

    @Override
    @Transactional
    public ProfileResponse createProfile(Long userId, ProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id " + userId + " не найден"));
        if (profileRepository.existsByUserId(userId)) {
            throw new UserServiceException("Профиль для пользователя id " + userId + " уже существует");
        }
        Profile profile = profileMapper.toEntity(request);
        profile.setUser(user);
        Profile saved = profileRepository.save(profile);
        log.info("Создан профиль для пользователя id={}", userId);
        return profileMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @Retryable(
            retryFor = {OptimisticLockingFailureException.class},
            backoff = @Backoff(delay = 100)
    )
    public ProfileResponse updateProfile(Long userId, ProfileRequest request) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id " + userId + " не найден"));
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException(userId));
        profileMapper.toEntity(request, profile);
        Profile updated = profileRepository.save(profile);
        log.info("Обновлён профиль для пользователя id={}", userId);
        return profileMapper.toResponse(updated);
    }

    @Recover
    public ProfileResponse recover(OptimisticLockingFailureException e, Long userId, ProfileRequest request) {
        log.error("Не удалось обновить профиль для пользователя id={} после попыток. Request: {}", userId, request);
        throw new UserServiceException("Конфликт версии при обновлении профиля. Повторите операцию позже.", e);
    }

    @Override
    public ProfileResponse findProfileByUserId(Long userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException(userId));
        return profileMapper.toResponse(profile);
    }

    @Override
    @Transactional
    public void deleteProfile(Long userId) {
        if (!profileRepository.existsByUserId(userId)) {
            throw new ProfileNotFoundException(userId);
        }
        profileRepository.deleteByUserId(userId);
        log.info("Удалён профиль для пользователя id={}", userId);
    }

    @Override
    public Page<ProfileResponse> findAllProfiles(Pageable pageable) {
        return profileRepository.findAll(pageable)
                .map(profileMapper::toResponse);
    }
}
