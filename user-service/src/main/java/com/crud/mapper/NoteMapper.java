package com.crud.mapper;

import com.crud.dto.NoteRequest;
import com.crud.dto.NoteResponse;
import com.crud.entity.Note;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/** Маппер заметок (MapStruct). */
@Mapper(config = UserServiceMapperConfig.class)
public interface NoteMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "content", source = "content")
    Note toEntity(NoteRequest request);

    NoteResponse toResponse(Note note);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "content", source = "content")
    Note toEntity(NoteRequest request, @MappingTarget Note existing);
}
