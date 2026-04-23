package com.crud.repository;

import com.crud.exception.DataAccessException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Абстрактный репозиторий, реализующий паттерн "Шаблонный метод" (Template Method).
 * <p>
 * Предоставляет общие методы для работы с транзакциями: {@link #executeInTransaction(Function)}
 * и {@link #executeInTransactionVoid(Consumer)}. Все конкретные репозитории должны наследовать этот класс.
 * </p>
 * <p>
 * Также содержит стандартные реализации {@link #findById(Object)} и {@link #findAll()},
 * которые могут быть переопределены при необходимости.
 * </p>
 *
 * @param <T>  тип сущности (например, User)
 * @param <I> тип идентификатора (например, Long)
 */
public abstract class AbstractRepository<T, I> {

    protected final SessionFactory sessionFactory;
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected AbstractRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Возвращает класс сущности (нужен для Hibernate-запросов).
     */
    protected abstract Class<T> getEntityClass();

    /**
     * Шаблонный метод для выполнения операций в транзакции с возвратом результата.
     * <p>
     * Использует try-with-resources для автоматического закрытия сессии.
     * </p>
     *
     * @param action функция, получающая Session и возвращающая результат
     * @param <R>    тип результата
     * @return результат выполнения action
     * @throws DataAccessException если произошла ошибка базы данных
     */
    protected <R> R executeInTransaction(Function<Session, R> action) {
        Session session = null;
        Transaction tx = null;
        try {
            session = sessionFactory.openSession();
            tx = session.beginTransaction();
            R result = action.apply(session);
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("Ошибка при выполнении транзакции", e);
            throw new DataAccessException("Ошибка доступа к данным: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Шаблонный метод для выполнения операций в транзакции без возврата результата (void).
     *
     * @param action потребитель, получающий Session
     * @throws DataAccessException если произошла ошибка базы данных
     */
    protected void executeInTransactionVoid(Consumer<Session> action) {
        executeInTransaction(session -> {
            action.accept(session);
            return null;
        });
    }

    /**
     * Находит сущность по идентификатору.
     *
     * @param id идентификатор
     * @return Optional с найденной сущностью или пустой Optional
     */
    public Optional<T> findById(I id) {
        return executeInTransaction(session -> Optional.ofNullable(session.find(getEntityClass(), id)));
    }

    /**
     * Возвращает все сущности данного типа.
     *
     * @return список всех сущностей (может быть пустым)
     */
    public List<T> findAll() {
        return executeInTransaction(session -> {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<T> cq = cb.createQuery(getEntityClass());
            cq.from(getEntityClass());
            return session.createQuery(cq).getResultList();
        });
    }
}