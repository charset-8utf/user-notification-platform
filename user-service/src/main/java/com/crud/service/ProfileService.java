package com.crud.service;

import com.crud.dto.ProfileRequest;
import com.crud.dto.ProfileResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Сервис профилей.
 */
public interface ProfileService {

    ProfileResponse createProfile(Long userId, ProfileRequest request);
    ProfileResponse updateProfile(Long userId, ProfileRequest request);
    ProfileResponse findProfileByUserId(Long userId);
    Page<ProfileResponse> findAllProfiles(Pageable pageable);
    void deleteProfile(Long userId);
}
