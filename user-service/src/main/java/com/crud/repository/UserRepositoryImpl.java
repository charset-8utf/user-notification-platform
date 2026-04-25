package com.crud.repository;

import com.crud.entity.User;
import com.crud.exception.DataAccessException;
import com.crud.exception.UserNotFoundException;
import org.hibernate.SessionFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.StaleObjectStateException;

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
     * Обновляет пользователя, выполняя слияние detached-сущности с persistent-контекстом.
     * <p>
     * При наличии поля {@code @Version} проверяется оптимистическая блокировка:
     * если версия переданного объекта не соответствует текущей версии в БД,
     * выбрасывается исключение с сообщением о конкурентном изменении.
     * </p>
     *
     * @param user сущность с обновлёнными данными (должна содержать id и актуальную версию)
     * @return обновлённая (присоединённая) сущность
     * @throws UserNotFoundException       если пользователь с данным id не существует
     * @throws DataAccessException         если произошла ошибка при обновлении,
     *                                     в том числе {@link StaleObjectStateException} (конкурентное изменение)
     */
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

    /**
     * Находит пользователя по адресу электронной почты.
     * <p>
     * Использует именованный запрос {@code User.findByEmail} для выполнения поиска.
     * Если пользователь с указанным email не найден, возвращает пустой {@link Optional}.
     * </p>
     *
     * @param email адрес электронной почты (уникальный)
     * @return Optional с найденным пользователем или пустой Optional
     */
    @Override
    public Optional<User> findByEmail(String email) {
        return executeInTransaction(session ->
                session.createNamedQuery("User.findByEmail", User.class)
                        .setParameter("email", email)
                        .uniqueResultOptional()
        );
    }
}