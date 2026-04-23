package com.crud.repository;

import com.crud.entity.User;
import com.crud.exception.DataAccessException;
import com.crud.exception.UserNotFoundException;
import org.hibernate.SessionFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Реализация репозитория пользователей на основе Hibernate.
 * <p>
 * Наследует {@link AbstractRepository} и реализует {@link UserRepository}.
 * Использует шаблонный метод для управления транзакциями.
 * </p>
 */
public class UserRepositoryImpl extends AbstractRepository<User, Long> implements UserRepository {

    private static final Logger log = LoggerFactory.getLogger(UserRepositoryImpl.class);

    /**
     * Конструктор, получающий SessionFactory из HibernateUtil.
     *
     * @param sessionFactory фабрика сессий
     */
    public UserRepositoryImpl(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @Override
    protected Class<User> getEntityClass() {
        return User.class;
    }

    /**
     * Сохраняет пользователя. При нарушении уникальности email выбрасывает DataAccessException.
     *
     * @param user сущность для сохранения
     * @return сохранённая сущность
     * @throws DataAccessException если email уже существует
     */
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

    /**
     * Находит пользователя по id. Возвращает Optional.
     */
    @Override
    public Optional<User> findById(Long id) {
        log.debug("Поиск пользователя по id: {}", id);
        return super.findById(id);
    }

    /**
     * Возвращает всех пользователей.
     */
    @Override
    public List<User> findAll() {
        log.debug("Запрос всех пользователей");
        return super.findAll();
    }

    /**
     * Обновляет пользователя. Использует Optional для проверки существования.
     * Если пользователь с таким id не существует, выбрасывает UserNotFoundException.
     *
     * @param user сущность с обновлёнными данными (должен содержать id)
     * @return обновлённая сущность
     * @throws UserNotFoundException если пользователь не найден
     */
    @Override
    public User update(User user) {
        return executeInTransaction(session ->
                Optional.ofNullable(session.find(User.class, user.getId()))
                        .map(existing -> {
                            existing.setName(user.getName());
                            existing.setEmail(user.getEmail());
                            existing.setAge(user.getAge());
                            session.merge(existing);
                            log.info("Пользователь обновлён: id={}, email={}", existing.getId(), existing.getEmail());
                            return existing;
                        })
                        .orElseThrow(() -> new UserNotFoundException(user.getId()))
        );
    }

    /**
     * Удаляет пользователя по id. Если пользователь не найден, ничего не происходит (логируется предупреждение).
     *
     * @param id идентификатор
     */
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
}