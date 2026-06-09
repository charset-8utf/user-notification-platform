package com.crud.service;

import com.crud.dto.NoteRequest;
import com.crud.dto.NoteResponse;
import com.crud.entity.Note;
import com.crud.entity.User;
import com.crud.exception.NoteNotFoundException;
import com.crud.exception.UserNotFoundException;
import com.crud.exception.ValidationException;
import com.crud.mapper.NoteMapper;
import com.crud.repository.NoteRepository;
import com.crud.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.CONCURRENT)
class NoteServiceEdgeCasesTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NoteMapper noteMapper;

    @InjectMocks
    private NoteServiceImpl noteService;

    @Test
    void createNote_WhenUserNotFound_ShouldThrowException() {
        NoteRequest request = new NoteRequest("Test content");
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteService.createNote(999L, request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void findNoteById_WhenNotFound_ShouldThrowException() {
        when(noteRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteService.findNoteById(1L, 999L))
                .isInstanceOf(NoteNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void findNoteById_WhenWrongOwner_ShouldThrowValidationException() {
        User owner = User.builder().name("Owner").build();
        owner.setId(2L);
        Note note = Note.builder().content("Test").user(owner).build();
        note.setId(1L);

        when(noteRepository.findById(1L)).thenReturn(Optional.of(note));

        assertThatThrownBy(() -> noteService.findNoteById(1L, 1L))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void updateNote_WhenNotFound_ShouldThrowException() {
        NoteRequest request = new NoteRequest("Updated content");
        when(noteRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteService.updateNote(1L, 999L, request))
                .isInstanceOf(NoteNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void deleteNote_WhenNoteExists_ShouldDelete() {
        Long userId = 1L;
        User user = User.builder().name("John").build();
        user.setId(userId);
        Note note = Note.builder().content("Test").user(user).build();
        note.setId(1L);

        when(noteRepository.findById(1L)).thenReturn(Optional.of(note));

        noteService.deleteNote(userId, 1L);

        verify(noteRepository).deleteById(1L);
    }

    @Test
    void deleteNote_WhenWrongOwner_ShouldThrowValidationException() {
        User owner = User.builder().name("Owner").build();
        owner.setId(2L);
        Note note = Note.builder().content("Test").user(owner).build();
        note.setId(1L);

        when(noteRepository.findById(1L)).thenReturn(Optional.of(note));

        assertThatThrownBy(() -> noteService.deleteNote(1L, 1L))
                .isInstanceOf(ValidationException.class);

        verify(noteRepository, never()).deleteById(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "A very long content that exceeds typical limits but should still be handled properly in the system without any issues"})
    void createNote_WithVariousContentLengths_ShouldWork(String content) {
        User user = User.builder().name("John").email("john@test.com").age(30).build();
        user.setId(1L);
        Note note = Note.builder().content(content).user(user).build();
        note.setId(1L);
        note.setCreatedAt(LocalDateTime.now());
        note.setUpdatedAt(LocalDateTime.now());

        NoteRequest request = new NoteRequest(content);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(noteMapper.toEntity(request)).thenReturn(Note.builder().content(content).build());
        when(noteRepository.save(any())).thenReturn(note);
        when(noteMapper.toResponse(any())).thenReturn(new NoteResponse(1L, content, LocalDateTime.now(), LocalDateTime.now()));

        NoteResponse result = noteService.createNote(1L, request);

        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo(content);
    }

    @ParameterizedTest
    @CsvSource({
            "1, true",
            "2, true",
            "999, false"
    })
    void existsById_ShouldReturnCorrectResult(Long id, boolean exists) {
        when(noteRepository.existsById(id)).thenReturn(exists);

        boolean result = noteRepository.existsById(id);

        assertThat(result).isEqualTo(exists);
    }
}
