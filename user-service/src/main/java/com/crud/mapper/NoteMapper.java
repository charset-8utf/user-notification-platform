package com.crud.mapper;

import com.crud.dto.NoteRequest;
import com.crud.dto.NoteResponse;
import com.crud.entity.Note;

/**
 * Маппер заметок.
 */
public interface NoteMapper {

    Note toEntity(NoteRequest request);

    NoteResponse toResponse(Note note);

    Note toEntity(NoteRequest request, Note existing);
}
