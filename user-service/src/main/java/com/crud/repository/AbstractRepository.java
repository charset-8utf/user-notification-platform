package com.crud.repository;

import com.crud.exception.DataAccessException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Базовый репозиторий с шаблонными методами транзакций.
 */
@Slf4j
public abstract class AbstractRepository<T, I> {

    protected final SessionFactory sessionFactory;

    protected AbstractRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    protected abstract Class<T> getEntityClass();

    protected <R> R executeInTransaction(Function<Session, R> action) {
        Session session = null;
        Transaction tx = null;
        try {
            session = sessionFactory.openSession();
            tx = session.beginTransaction();
            R result = action.apply(session);
            tx.commit();
            return result;
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new DataAccessException("Ошибка транзакции: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    protected void executeInTransactionVoid(Consumer<Session> action) {
        executeInTransaction(session -> {
            action.accept(session);
            return null;
        });
    }

    public Optional<T> findById(I id) {
        return executeInTransaction(session -> Optional.ofNullable(session.find(getEntityClass(), id)));
    }
}
