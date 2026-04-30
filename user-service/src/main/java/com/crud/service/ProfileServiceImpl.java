package com.crud.service;

import com.crud.dto.Page;
import com.crud.dto.Pageable;
import com.crud.dto.ProfileRequest;
import com.crud.dto.ProfileResponse;
import com.crud.entity.Profile;
import com.crud.entity.User;
import com.crud.exception.DataAccessException;
import com.crud.exception.ProfileNotFoundException;
import com.crud.exception.UserNotFoundException;
import com.crud.exception.ValidationException;
import com.crud.mapper.ProfileMapper;
import com.crud.mapper.ProfileMapperImpl;
import com.crud.repository.ProfileRepository;
import com.crud.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

/** Сервис профилей с валидацией и retry. */
@Slf4j
public class ProfileServiceImpl extends AbstractService implements ProfileService {
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final ProfileMapper profileMapper;

    public ProfileServiceImpl(ProfileRepository profileRepository, UserRepository userRepository) {
        this(profileRepository, userRepository, new ProfileMapperImpl());
    }

    public ProfileServiceImpl(ProfileRepository profileRepository, UserRepository userRepository, ProfileMapper profileMapper) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.profileMapper = profileMapper;
    }

    private void validate(ProfileRequest request) {
        if (request.phone() != null && request.phone().length() > 20) {
            throw new ValidationException("Номер телефона не может быть длиннее 20 символов");
        }
    }

    @Override
    public ProfileResponse createProfile(Long userId, ProfileRequest request) {
        validate(request);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id " + userId + " не найден"));
        if (profileRepository.findByUserId(userId).isPresent()) {
            throw new DataAccessException("Профиль для пользователя id " + userId + " уже существует", null);
        }
        Profile profile = Profile.builder()
                .phone(request.phone())
                .address(request.address())
                .user(user)
                .build();
        Profile saved = profileRepository.save(profile);
        log.info("Создан профиль для пользователя id={}", userId);
        return profileMapper.toResponse(saved);
    }

    @Override
    public ProfileResponse updateProfile(Long userId, ProfileRequest request) {
        validate(request);
        return executeWithRetry(() -> {
            userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("Пользователь с id " + userId + " не найден"));
            Profile profile = profileRepository.findByUserId(userId)
                    .orElseThrow(() -> new ProfileNotFoundException(userId));
            profileMapper.toEntity(request, profile);
            Profile updated = profileRepository.update(profile);
            log.info("Обновлён профиль для пользователя id={}", userId);
            return profileMapper.toResponse(updated);
        });
    }

    @Override
    public ProfileResponse findProfileByUserId(Long userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException(userId));
        return profileMapper.toResponse(profile);
    }

    @Override
    public void deleteProfile(Long userId) {
        profileRepository.deleteByUserId(userId);
        log.info("Удалён профиль для пользователя id={}", userId);
    }

    @Override
    public Page<ProfileResponse> findAllProfiles(Pageable pageable) {
        Page<Profile> page = profileRepository.findAll(pageable);
        var responses = page.content().stream()
                .map(profileMapper::toResponse)
                .toList();
        return new Page<>(responses, page.totalElements(), page.page(), page.size());
    }
}
