package com.crud.mapper;

import com.crud.dto.ProfileRequest;
import com.crud.dto.ProfileResponse;
import com.crud.entity.Profile;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/** Маппер профилей (MapStruct). */
@Mapper(config = UserServiceMapperConfig.class)
public interface ProfileMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "address", source = "address")
    Profile toEntity(ProfileRequest request);

    @Mapping(source = "user.id", target = "userId")
    ProfileResponse toResponse(Profile profile);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "address", source = "address")
    Profile toEntity(ProfileRequest request, @MappingTarget Profile existing);
}
