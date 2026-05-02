package com.crud.mapper;

import com.crud.dto.NoteRequest;
import com.crud.dto.NoteResponse;
import com.crud.entity.Note;

import java.util.List;

/**
 * Реализация маппера заметок.
 */
public class NoteMapperImpl implements NoteMapper {

    @Override
    public Note toEntity(NoteRequest request) {
        return Note.builder()
                .content(request.content())
                .build();
    }

    @Override
    public NoteResponse toResponse(Note note) {
        return new NoteResponse(
                note.getId(),
                note.getContent(),
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }

    @Override
    public List<NoteResponse> toResponseList(List<Note> notes) {
        if (notes == null) {
            return List.of();
        }
        return notes.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public Note toEntity(NoteRequest request, Note existing) {
        existing.setContent(request.content());
        return existing;
    }
}
