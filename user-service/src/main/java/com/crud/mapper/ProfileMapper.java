package com.crud.mapper;

import com.crud.dto.ProfileRequest;
import com.crud.dto.ProfileResponse;
import com.crud.entity.Profile;

import java.util.List;

/**
 * Маппер профилей.
 */
public interface ProfileMapper {

    Profile toEntity(ProfileRequest request);
    ProfileResponse toResponse(Profile profile);
    Profile toEntity(ProfileRequest request, Profile existing);
    List<ProfileResponse> toResponseList(List<Profile> profiles);
}
