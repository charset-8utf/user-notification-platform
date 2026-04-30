package com.crud.repository;

import com.crud.dto.Page;
import com.crud.dto.Pageable;
import com.crud.entity.User;
import com.crud.exception.DataAccessException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.StaleObjectStateException;

import java.util.List;
import java.util.Optional;

/**
 * Реализация репозитория пользователей.
 */
@Slf4j
public class UserRepositoryImpl extends AbstractRepository<User, Long> implements UserRepository {

    public UserRepositoryImpl(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @Override
    protected Class<User> getEntityClass() {
        return User.class;
    }

    @Override
    public User save(User user) {
        try {
            return executeInTransaction(session -> {
                session.persist(user);
                log.info("Пользователь сохранён: id={}, email={}", user.getId(), user.getEmail());
                return user;
            });
        } catch (DataAccessException e) {
            Optional.of(e.getCause())
                    .filter(ConstraintViolationException.class::isInstance)
                    .ifPresent(cause -> {
                        log.warn("Попытка сохранить пользователя с уже существующим email: {}", user.getEmail());
                        throw new DataAccessException("Пользователь с email " + user.getEmail() + " уже существует", cause);
                    });
            throw e;
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        log.debug("Поиск пользователя по id: {}", id);
        return super.findById(id);
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        log.debug("Запрос пользователей с пагинацией: page={}, size={}", pageable.page(), pageable.size());
        return executeInTransaction(session -> {
            Long total = session.createQuery("SELECT COUNT(u) FROM User u", Long.class)
                    .getSingleResult();
            
            List<User> content = session.createQuery("FROM User u ORDER BY u.id", User.class)
                    .setFirstResult(pageable.offset())
                    .setMaxResults(pageable.size())
                    .getResultList();

            return new Page<>(content, total, pageable.page(), pageable.size());
        });
    }

    @Override
    public User update(User user) {
        try {
            return executeInTransaction(session -> {
                User merged = session.merge(user);
                log.info("Пользователь обновлён: id={}, email={}", merged.getId(), merged.getEmail());
                return merged;
            });
        } catch (DataAccessException e) {
            if (e.getCause() instanceof StaleObjectStateException) {
                throw new DataAccessException("Пользователь с id " + user.getId() + " был изменён другим пользователем. Повторите операцию.", e.getCause());
            }
            throw e;
        }
    }

    @Override
    public void deleteById(Long id) {
        executeInTransactionVoid(session ->
                Optional.ofNullable(session.find(User.class, id))
                        .ifPresentOrElse(
                                user -> {
                                    session.remove(user);
                                    log.info("Пользователь удалён: id={}", id);
                                },
                                () -> log.warn("Попытка удалить несуществующего пользователя с id={}", id)
                        )
        );
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return executeInTransaction(session ->
                session.createNamedQuery("User.findByEmail", User.class)
                        .setParameter("email", email)
                        .uniqueResultOptional()
        );
    }

    @Override
    public Optional<User> findByIdWithLock(Long id) {
        return executeInTransaction(session -> {
            User user = session.find(User.class, id, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
            if (user != null) {
                log.debug("Пользователь заблокирован для обновления: id={}", id);
            }
            return Optional.ofNullable(user);
        });
    }
}
