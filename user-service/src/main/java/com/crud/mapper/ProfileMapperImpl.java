package com.crud.mapper;

import com.crud.dto.ProfileRequest;
import com.crud.dto.ProfileResponse;
import com.crud.entity.Profile;
import org.springframework.stereotype.Component;

@Component
public class ProfileMapperImpl implements ProfileMapper {

    @Override
    public Profile toEntity(ProfileRequest request) {
        return Profile.builder()
                .phone(request.phone())
                .address(request.address())
                .build();
    }

    @Override
    public ProfileResponse toResponse(Profile profile) {
        Long userId = profile.getUser() != null ? profile.getUser().getId() : null;
        return new ProfileResponse(
                profile.getId(),
                userId,
                profile.getPhone(),
                profile.getAddress(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }

    @Override
    public Profile toEntity(ProfileRequest request, Profile existing) {
        existing.setPhone(request.phone());
        existing.setAddress(request.address());
        return existing;
    }
}
