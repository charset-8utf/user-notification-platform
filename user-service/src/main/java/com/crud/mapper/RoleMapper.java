package com.crud.mapper;

import com.crud.dto.RoleRequest;
import com.crud.dto.RoleResponse;
import com.crud.entity.Role;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/** Маппер ролей (MapStruct). */
@Mapper(config = UserServiceMapperConfig.class)
public interface RoleMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "name", source = "name")
    Role toEntity(RoleRequest request);

    RoleResponse toResponse(Role role);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "name", source = "name")
    Role toEntity(RoleRequest request, @MappingTarget Role existing);
}
