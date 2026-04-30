package com.crud.service;

import com.crud.dto.NoteRequest;
import com.crud.dto.NoteResponse;
import com.crud.dto.Page;
import com.crud.dto.Pageable;
import com.crud.entity.Note;
import com.crud.entity.User;
import com.crud.exception.NoteNotFoundException;
import com.crud.exception.UserNotFoundException;
import com.crud.exception.ValidationException;
import com.crud.mapper.NoteMapper;
import com.crud.mapper.NoteMapperImpl;
import com.crud.repository.NoteRepository;
import com.crud.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/** Сервис заметок с валидацией и retry. */
@Slf4j
public class NoteServiceImpl extends AbstractService implements NoteService {
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final NoteMapper noteMapper;

    public NoteServiceImpl(NoteRepository noteRepository, UserRepository userRepository) {
        this(noteRepository, userRepository, new NoteMapperImpl());
    }

    public NoteServiceImpl(NoteRepository noteRepository, UserRepository userRepository, NoteMapper noteMapper) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
        this.noteMapper = noteMapper;
    }

    private void validate(NoteRequest request) {
        if (request.content() == null || request.content().isBlank()) {
            throw new ValidationException("Текст заметки не может быть пустым");
        }
    }

    @Override
    public NoteResponse createNote(Long userId, NoteRequest request) {
        validate(request);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id " + userId + " не найден"));
        Note note = Note.builder()
                .content(request.content())
                .user(user)
                .build();
        Note saved = noteRepository.save(note);
        log.info("Создана заметка id={} для пользователя id={}", saved.getId(), userId);
        return noteMapper.toResponse(saved);
    }

    @Override
    public NoteResponse findNoteById(Long id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new NoteNotFoundException(id));
        return noteMapper.toResponse(note);
    }

    @Override
    public Page<NoteResponse> findNotesByUserId(Long userId, Pageable pageable) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id " + userId + " не найден"));
        Page<Note> notePage = noteRepository.findByUserId(userId, pageable);
        List<NoteResponse> content = notePage.content().stream()
                .map(noteMapper::toResponse)
                .toList();
        return new Page<>(content, notePage.totalElements(), notePage.page(), notePage.size());
    }

    @Override
    public NoteResponse updateNote(Long id, NoteRequest request) {
        validate(request);
        return executeWithRetry(() -> {
            Note note = noteRepository.findById(id)
                    .orElseThrow(() -> new NoteNotFoundException(id));
            noteMapper.toEntity(request, note);
            Note updated = noteRepository.update(note);
            log.info("Обновлена заметка id={}", updated.getId());
            return noteMapper.toResponse(updated);
        });
    }

    @Override
    public void deleteNote(Long id) {
        noteRepository.deleteById(id);
        log.info("Удалена заметка id={}", id);
    }
}
