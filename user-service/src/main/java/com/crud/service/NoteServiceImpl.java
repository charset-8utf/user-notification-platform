package com.crud.service;

import com.crud.dto.NoteRequest;
import com.crud.dto.NoteResponse;
import com.crud.entity.Note;
import com.crud.entity.User;
import com.crud.exception.NoteNotFoundException;
import com.crud.exception.UserNotFoundException;
import com.crud.exception.UserServiceException;
import com.crud.exception.ValidationException;
import com.crud.mapper.NoteMapper;
import com.crud.repository.NoteRepository;
import com.crud.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Сервис заметок с валидацией и retry. */
@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final NoteMapper noteMapper;

    @Override
    @Transactional
    public NoteResponse createNote(Long userId, NoteRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id " + userId + " не найден"));
        Note note = noteMapper.toEntity(request);
        note.setUser(user);
        Note saved = noteRepository.save(note);
        log.info("Создана заметка id={} для пользователя id={}", saved.getId(), userId);
        return noteMapper.toResponse(saved);
    }

    @Override
    public NoteResponse findNoteById(Long userId, Long id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new NoteNotFoundException(id));
        validateOwnership(note, userId);
        return noteMapper.toResponse(note);
    }

    @Override
    public Page<NoteResponse> findNotesByUserId(Long userId, Pageable pageable) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id " + userId + " не найден"));
        return noteRepository.findByUserId(userId, pageable)
                .map(noteMapper::toResponse);
    }

    @Override
    @Transactional
    @Retryable(
            retryFor = {OptimisticLockingFailureException.class},
            backoff = @Backoff(delay = 100)
    )
    public NoteResponse updateNote(Long userId, Long id, NoteRequest request) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new NoteNotFoundException(id));
        validateOwnership(note, userId);
        noteMapper.toEntity(request, note);
        Note updated = noteRepository.save(note);
        log.info("Обновлена заметка id={}", updated.getId());
        return noteMapper.toResponse(updated);
    }

    @Recover
    public NoteResponse recover(OptimisticLockingFailureException e, Long userId, Long id, NoteRequest request) {
        log.error("Не удалось обновить заметку с id={} после попыток. Request: {}", id, request);
        throw new UserServiceException("Конфликт версии при обновлении заметки. Повторите операцию позже.", e);
    }

    @Override
    @Transactional
    public void deleteNote(Long userId, Long id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new NoteNotFoundException(id));
        validateOwnership(note, userId);
        noteRepository.deleteById(id);
        log.info("Удалена заметка id={}", id);
    }

    private void validateOwnership(Note note, Long userId) {
        if (!note.getUser().getId().equals(userId)) {
            throw new ValidationException("Заметка с id " + note.getId() + " не принадлежит пользователю с id " + userId);
        }
    }
}
