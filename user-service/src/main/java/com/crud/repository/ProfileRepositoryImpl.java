package com.crud.repository;

import com.crud.dto.Page;
import com.crud.dto.Pageable;
import com.crud.entity.Profile;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import java.util.Optional;

/**
 * Реализация репозитория для профилей.
 */
@Slf4j
public class ProfileRepositoryImpl extends AbstractRepository<Profile, Long> implements ProfileRepository {

    public ProfileRepositoryImpl(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @Override
    protected Class<Profile> getEntityClass() {
        return Profile.class;
    }

    @Override
    public Profile save(Profile profile) {
        return executeInTransaction(session -> {
            session.persist(profile);
            return profile;
        });
    }

    @Override
    public Profile update(Profile profile) {
        return executeInTransaction(session -> session.merge(profile));
    }

    @Override
    public void deleteById(Long id) {
        executeInTransactionVoid(session ->
            Optional.ofNullable(session.find(Profile.class, id)).ifPresent(session::remove)
        );
    }

    @Override
    public Optional<Profile> findByUserId(Long userId) {
        return executeInTransaction(session -> {
            String hql = "SELECT p FROM Profile p WHERE p.user.id = :userId";
            return session.createQuery(hql, Profile.class)
                    .setParameter("userId", userId)
                    .uniqueResultOptional();
        });
    }

    @Override
    public void deleteByUserId(Long userId) {
        executeInTransactionVoid(session -> {
            String hql = "DELETE FROM Profile WHERE user.id = :userId";
            session.createMutationQuery(hql)
                    .setParameter("userId", userId)
                    .executeUpdate();
        });
    }

    @Override
    public Page<Profile> findAll(Pageable pageable) {
        return executeInTransaction(session -> {
            String countHql = "SELECT COUNT(p) FROM Profile p";
            long total = session.createQuery(countHql, Long.class)
                    .getSingleResult();

            String hql = "SELECT p FROM Profile p ORDER BY p.id";
            var query = session.createQuery(hql, Profile.class)
                    .setFirstResult(pageable.offset())
                    .setMaxResults(pageable.size());

            var content = query.getResultList();

            return new Page<>(content, total, pageable.page(), pageable.size());
        });
    }
}
