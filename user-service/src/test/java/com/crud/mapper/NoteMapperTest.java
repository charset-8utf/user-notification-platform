package com.crud.mapper;

import com.crud.dto.NoteRequest;
import com.crud.dto.NoteResponse;
import com.crud.entity.Note;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Execution(ExecutionMode.CONCURRENT)
class NoteMapperTest {

    private final NoteMapper noteMapper = new NoteMapperImpl();

    @Test
    void toResponse_FromEntity_ShouldMapAllFields() {
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();
        Note note = Note.builder().content("Test content").build();
        note.setId(1L);
        note.setCreatedAt(createdAt);
        note.setUpdatedAt(updatedAt);

        NoteResponse response = noteMapper.toResponse(note);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.content()).isEqualTo("Test content");
        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.updatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void toEntity_WithExistingEntity_ShouldUpdateContent() {
        Note existing = Note.builder().content("Old content").build();
        existing.setId(1L);
        NoteRequest request = new NoteRequest("New content");

        Note result = noteMapper.toEntity(request, existing);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getContent()).isEqualTo("New content");
    }
}
