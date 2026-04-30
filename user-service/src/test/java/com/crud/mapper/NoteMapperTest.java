package com.crud.mapper;

import com.crud.dto.NoteRequest;
import com.crud.dto.NoteResponse;
import com.crud.entity.Note;
import com.crud.entity.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NoteMapperTest {

    private final NoteMapper mapper = new NoteMapperImpl();

    @Test
    void toEntity_ShouldMapRequestToNote() {
        NoteRequest request = new NoteRequest("Test content");

        Note result = mapper.toEntity(request);

        assertEquals("Test content", result.getContent());
    }

    @Test
    void toResponse_ShouldMapNoteToResponse() {
        User user = User.builder().name("John").email("john@example.com").age(25).build();
        user.setId(1L);
        Note note = Note.builder().content("Test content").user(user).build();
        note.setId(1L);
        note.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
        note.setUpdatedAt(LocalDateTime.of(2025, 1, 2, 12, 0));

        NoteResponse result = mapper.toResponse(note);

        assertEquals(1L, result.id());
        assertEquals("Test content", result.content());
        assertNotNull(result.createdAt());
        assertNotNull(result.updatedAt());
    }

    @Test
    void toResponseList_ShouldMapListOfNotes() {
        User user = User.builder().name("John").email("john@example.com").age(25).build();
        user.setId(1L);
        Note note1 = Note.builder().content("Note 1").user(user).build();
        note1.setId(1L);
        note1.setCreatedAt(LocalDateTime.now());
        Note note2 = Note.builder().content("Note 2").user(user).build();
        note2.setId(2L);
        note2.setCreatedAt(LocalDateTime.now());

        List<NoteResponse> result = mapper.toResponseList(List.of(note1, note2));

        assertEquals(2, result.size());
        assertEquals("Note 1", result.get(0).content());
        assertEquals("Note 2", result.get(1).content());
    }

    @Test
    void toResponseList_WithNull_ShouldReturnEmptyList() {
        List<NoteResponse> result = mapper.toResponseList(null);

        assertTrue(result.isEmpty());
    }

    @Test
    void toEntity_WithExisting_ShouldUpdateNote() {
        User user = User.builder().name("John").email("john@example.com").age(25).build();
        user.setId(1L);
        Note existing = Note.builder().content("Old content").user(user).build();
        existing.setId(1L);

        NoteRequest request = new NoteRequest("New content");
        Note result = mapper.toEntity(request, existing);

        assertEquals("New content", result.getContent());
        assertSame(existing, result);
    }
}
