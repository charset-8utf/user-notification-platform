package com.crud.repository;

import com.crud.dto.Page;
import com.crud.dto.Pageable;
import com.crud.entity.Note;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Optional;

/**
 * Реализация репозитория заметок.
 */
@Slf4j
public class NoteRepositoryImpl extends AbstractRepository<Note, Long> implements NoteRepository {

    public NoteRepositoryImpl(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @Override
    protected Class<Note> getEntityClass() {
        return Note.class;
    }

    @Override
    public Note save(Note note) {
        return executeInTransaction(session -> {
            session.persist(note);
            return note;
        });
    }

    @Override
    public Note update(Note note) {
        return executeInTransaction(session -> session.merge(note));
    }

    @Override
    public void deleteById(Long id) {
        executeInTransactionVoid(session ->
            Optional.ofNullable(session.find(Note.class, id))
                    .ifPresent(session::remove)
        );
    }

    @Override
    public Page<Note> findAll(Pageable pageable) {
        return executeInTransaction(session -> {
            Long total = session.createQuery("SELECT COUNT(n) FROM Note n", Long.class)
                    .getSingleResult();

            List<Note> content = session.createQuery("FROM Note n ORDER BY n.id", Note.class)
                    .setFirstResult(pageable.offset())
                    .setMaxResults(pageable.size())
                    .getResultList();

            return new Page<>(content, total, pageable.page(), pageable.size());
        });
    }

    @Override
    public Page<Note> findByUserId(Long userId, Pageable pageable) {
        return executeInTransaction(session -> {
            Long total = session.createQuery("SELECT COUNT(n) FROM Note n WHERE n.user.id = :userId", Long.class)
                    .setParameter("userId", userId)
                    .getSingleResult();

            List<Note> content = session.createQuery("FROM Note n WHERE n.user.id = :userId ORDER BY n.id", Note.class)
                    .setParameter("userId", userId)
                    .setFirstResult(pageable.offset())
                    .setMaxResults(pageable.size())
                    .getResultList();

            return new Page<>(content, total, pageable.page(), pageable.size());
        });
    }
}
