package com.crud.mapper;

import com.crud.dto.ProfileRequest;
import com.crud.dto.ProfileResponse;
import com.crud.entity.Profile;

public interface ProfileMapper {

    Profile toEntity(ProfileRequest request);

    ProfileResponse toResponse(Profile profile);

    Profile toEntity(ProfileRequest request, Profile existing);
}
