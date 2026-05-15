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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.CONCURRENT)
class NoteServiceImplTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NoteMapper noteMapper;

    @InjectMocks
    private NoteServiceImpl noteService;

    @Test
    void createNote_ShouldSaveAndReturnResponse() {
        Long userId = 1L;
        NoteRequest request = new NoteRequest("Test note content");
        User user = User.builder().name("John").email("john@example.com").age(30).build();
        user.setId(userId);

        Note savedNote = Note.builder().content("Test note content").user(user).build();
        savedNote.setId(1L);
        savedNote.setCreatedAt(LocalDateTime.now());

        NoteResponse expected = new NoteResponse(1L, "Test note content", savedNote.getCreatedAt(), savedNote.getCreatedAt());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(noteMapper.toEntity(request)).thenReturn(Note.builder().content("Test note content").build());
        when(noteRepository.save(any(Note.class))).thenReturn(savedNote);
        when(noteMapper.toResponse(savedNote)).thenReturn(expected);

        NoteResponse actual = noteService.createNote(userId, request);

        assertThat(actual).isEqualTo(expected);
        verify(userRepository).findById(userId);
        verify(noteRepository).save(any(Note.class));
    }

    @Test
    void createNote_WhenUserNotExists_ShouldThrowUserNotFoundException() {
        Long userId = 999L;
        NoteRequest request = new NoteRequest("Test content");
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteService.createNote(userId, request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(String.valueOf(userId));
    }

    @Test
    void findNoteById_WhenExists_ShouldReturnResponse() {
        Long userId = 1L;
        Long noteId = 1L;
        User user = User.builder().name("John").build();
        user.setId(userId);

        Note note = Note.builder().content("Test content").user(user).build();
        note.setId(noteId);
        note.setCreatedAt(LocalDateTime.now());

        NoteResponse expected = new NoteResponse(noteId, "Test content", note.getCreatedAt(), note.getCreatedAt());

        when(noteRepository.findById(noteId)).thenReturn(Optional.of(note));
        when(noteMapper.toResponse(note)).thenReturn(expected);

        NoteResponse actual = noteService.findNoteById(userId, noteId);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void findNoteById_WhenNotExists_ShouldThrowNoteNotFoundException() {
        Long noteId = 999L;
        when(noteRepository.findById(noteId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteService.findNoteById(1L, noteId))
                .isInstanceOf(NoteNotFoundException.class)
                .hasMessageContaining(String.valueOf(noteId));
    }

    @Test
    void findNoteById_WhenNoteBelongsToDifferentUser_ShouldThrowValidationException() {
        Long userId = 1L;
        Long otherUserId = 2L;
        Long noteId = 1L;

        User owner = User.builder().name("Owner").build();
        owner.setId(otherUserId);

        Note note = Note.builder().content("Test content").user(owner).build();
        note.setId(noteId);

        when(noteRepository.findById(noteId)).thenReturn(Optional.of(note));

        assertThatThrownBy(() -> noteService.findNoteById(userId, noteId))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("не принадлежит пользователю");
    }

    @Test
    void findNotesByUserId_ShouldReturnPage() {
        Long userId = 1L;
        User user = User.builder().name("John").build();
        user.setId(userId);

        Note note = Note.builder().content("Test").user(user).build();
        note.setId(1L);

        NoteResponse response = new NoteResponse(1L, "Test", LocalDateTime.now(), LocalDateTime.now());
        Page<Note> notePage = new PageImpl<>(List.of(note));
        PageRequest pageRequest = PageRequest.of(0, 10);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(noteRepository.findByUserId(userId, pageRequest)).thenReturn(notePage);
        when(noteMapper.toResponse(note)).thenReturn(response);

        Page<NoteResponse> result = noteService.findNotesByUserId(userId, pageRequest);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void deleteNote_ShouldDelete() {
        Long userId = 1L;
        Long noteId = 1L;
        User user = User.builder().name("John").build();
        user.setId(userId);

        Note note = Note.builder().content("Test").user(user).build();
        note.setId(noteId);

        when(noteRepository.findById(noteId)).thenReturn(Optional.of(note));
        doNothing().when(noteRepository).deleteById(noteId);

        noteService.deleteNote(userId, noteId);

        verify(noteRepository).deleteById(noteId);
    }

    @Test
    void deleteNote_WhenNoteBelongsToDifferentUser_ShouldThrowValidationException() {
        Long userId = 1L;
        Long otherUserId = 2L;
        Long noteId = 1L;

        User owner = User.builder().name("Owner").build();
        owner.setId(otherUserId);

        Note note = Note.builder().content("Test").user(owner).build();
        note.setId(noteId);

        when(noteRepository.findById(noteId)).thenReturn(Optional.of(note));

        assertThatThrownBy(() -> noteService.deleteNote(userId, noteId))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("не принадлежит пользователю");

        verify(noteRepository, never()).deleteById(any());
    }
}
