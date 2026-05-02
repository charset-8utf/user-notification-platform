package com.crud.service;

import com.crud.dto.NoteRequest;
import com.crud.dto.NoteResponse;
import com.crud.entity.Note;
import com.crud.entity.User;
import com.crud.exception.DataAccessException;
import com.crud.exception.NoteNotFoundException;
import com.crud.exception.UserNotFoundException;
import com.crud.exception.ValidationException;
import com.crud.repository.NoteRepository;
import com.crud.repository.UserRepository;
import org.hibernate.StaleObjectStateException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteServiceImplTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    void createNote_ShouldCreateAndReturnResponse() {
        NoteServiceImpl service = new NoteServiceImpl(noteRepository, userRepository);
        User user = User.builder().name("John").email("john@example.com").age(25).build();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Note note = Note.builder().content("Test content").user(user).build();
        note.setId(1L);
        when(noteRepository.save(any(Note.class))).thenReturn(note);

        NoteResponse result = service.createNote(1L, new NoteRequest("Test content"));

        assertEquals(1L, result.id());
        assertEquals("Test content", result.content());
    }

    @Test
    void createNote_WhenUserNotFound_ShouldThrow() {
        NoteServiceImpl service = new NoteServiceImpl(noteRepository, userRepository);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NoteRequest request = new NoteRequest("Test");
        assertThrows(UserNotFoundException.class, () -> service.createNote(1L, request));
    }

    @Test
    void createNote_WhenContentBlank_ShouldThrowValidation() {
        NoteServiceImpl service = new NoteServiceImpl(noteRepository, userRepository);

        NoteRequest blankRequest = new NoteRequest("");
        NoteRequest spaceRequest = new NoteRequest("   ");
        NoteRequest nullRequest = new NoteRequest(null);
        assertThrows(ValidationException.class, () -> service.createNote(1L, blankRequest));
        assertThrows(ValidationException.class, () -> service.createNote(1L, spaceRequest));
        assertThrows(ValidationException.class, () -> service.createNote(1L, nullRequest));
    }

    @Test
    void findNoteById_WhenExists_ShouldReturnResponse() {
        NoteServiceImpl service = new NoteServiceImpl(noteRepository, userRepository);
        User user = User.builder().name("John").email("john@example.com").age(25).build();
        user.setId(1L);
        Note note = Note.builder().content("Test content").user(user).build();
        note.setId(1L);
        note.setCreatedAt(LocalDateTime.now());
        note.setUpdatedAt(LocalDateTime.now());
        when(noteRepository.findById(1L)).thenReturn(Optional.of(note));

        NoteResponse result = service.findNoteById(1L);

        assertEquals(1L, result.id());
    }

    @Test
    void findNoteById_WhenNotFound_ShouldThrow() {
        NoteServiceImpl service = new NoteServiceImpl(noteRepository, userRepository);
        when(noteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoteNotFoundException.class, () -> service.findNoteById(1L));
    }

    @Test
    void updateNote_ShouldUpdateAndReturnResponse() {
        NoteServiceImpl service = new NoteServiceImpl(noteRepository, userRepository);
        User user = User.builder().name("John").email("john@example.com").age(25).build();
        user.setId(1L);
        Note note = Note.builder().content("Old content").user(user).build();
        note.setId(1L);
        when(noteRepository.findById(1L)).thenReturn(Optional.of(note));
        when(noteRepository.update(any(Note.class))).thenAnswer(inv -> inv.getArgument(0));

        NoteResponse result = service.updateNote(1L, new NoteRequest("New content"));

        assertEquals(1L, result.id());
    }

    @Test
    void updateNote_WhenNotFound_ShouldThrow() {
        NoteServiceImpl service = new NoteServiceImpl(noteRepository, userRepository);
        when(noteRepository.findById(1L)).thenReturn(Optional.empty());

        NoteRequest updateRequest = new NoteRequest("New content");
        assertThrows(NoteNotFoundException.class, () -> service.updateNote(1L, updateRequest));
    }

    @Test
    void deleteNote_ShouldCallRepositoryDelete() {
        NoteServiceImpl service = new NoteServiceImpl(noteRepository, userRepository);
        doNothing().when(noteRepository).deleteById(1L);

        service.deleteNote(1L);

        verify(noteRepository).deleteById(1L);
    }

    @Test
    void updateNote_WithOptimisticLockException_ShouldRetryAndSucceed() {
        NoteServiceImpl service = new NoteServiceImpl(noteRepository, userRepository);
        User user = User.builder().name("John").email("john@example.com").age(25).build();
        user.setId(1L);
        Note note = Note.builder().content("Old").user(user).build();
        note.setId(1L);
        when(noteRepository.findById(1L))
                .thenReturn(Optional.of(note))
                .thenReturn(Optional.of(note))
                .thenReturn(Optional.of(note));
        when(noteRepository.update(any(Note.class)))
                .thenThrow(new DataAccessException("Optimistic lock", new StaleObjectStateException("Note", 1L)))
                .thenThrow(new DataAccessException("Optimistic lock", new StaleObjectStateException("Note", 1L)))
                .thenAnswer(inv -> inv.getArgument(0));

        NoteResponse result = service.updateNote(1L, new NoteRequest("New"));

        assertNotNull(result);
        verify(noteRepository, times(3)).update(any(Note.class));
    }
}
