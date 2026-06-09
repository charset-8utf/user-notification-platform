package com.crud.mapper;

import com.crud.dto.NoteRequest;
import com.crud.dto.NoteResponse;
import com.crud.entity.Note;
import org.springframework.stereotype.Component;

/**
 * Реализация маппера заметок.
 */
@Component
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
    public Note toEntity(NoteRequest request, Note existing) {
        existing.setContent(request.content());
        return existing;
    }
}
