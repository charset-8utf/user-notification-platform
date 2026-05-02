package com.crud.mapper;

import com.crud.dto.NoteRequest;
import com.crud.dto.NoteResponse;
import com.crud.entity.Note;

import java.util.List;

/**
 * Маппер заметок.
 */
public interface NoteMapper {

    Note toEntity(NoteRequest request);
    NoteResponse toResponse(Note note);
    List<NoteResponse> toResponseList(List<Note> notes);
    Note toEntity(NoteRequest request, Note existing);
}
