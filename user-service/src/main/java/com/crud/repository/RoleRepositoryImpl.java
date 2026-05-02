package com.crud.repository;

import com.crud.dto.Page;
import com.crud.dto.Pageable;
import com.crud.entity.Role;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import java.util.List;
import java.util.Optional;

/**
 * Реализация репозитория ролей.
 */
@Slf4j
public class RoleRepositoryImpl extends AbstractRepository<Role, Long> implements RoleRepository {

    public RoleRepositoryImpl(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @Override
    protected Class<Role> getEntityClass() {
        return Role.class;
    }

    @Override
    public Role save(Role role) {
        return executeInTransaction(session -> {
            session.persist(role);
            return role;
        });
    }

    @Override
    public Role update(Role role) {
        return executeInTransaction(session -> session.merge(role));
    }

    @Override
    public void deleteById(Long id) {
        executeInTransactionVoid(session ->
                Optional.ofNullable(session.find(Role.class, id)).ifPresent(session::remove)
        );
    }

    @Override
    public void assignRoleToUser(Long userId, Long roleId) {
        executeInTransactionVoid(session -> {
            String sql = "INSERT INTO user_role (user_id, role_id) VALUES (:userId, :roleId)";
            session.createNativeQuery(sql)
                    .setParameter("userId", userId)
                    .setParameter("roleId", roleId)
                    .executeUpdate();
        });
    }

    @Override
    public void removeRoleFromUser(Long userId, Long roleId) {
        executeInTransactionVoid(session -> {
            String sql = "DELETE FROM user_role WHERE user_id = :userId AND role_id = :roleId";
            session.createNativeQuery(sql)
                    .setParameter("userId", userId)
                    .setParameter("roleId", roleId)
                    .executeUpdate();
        });
    }

    @Override
    public Page<Role> findAll(Pageable pageable) {
        return executeInTransaction(session -> {
            Long total = session.createQuery("SELECT COUNT(r) FROM Role r", Long.class)
                    .getSingleResult();

            List<Role> content = session.createQuery("FROM Role r ORDER BY r.id", Role.class)
                    .setFirstResult(pageable.offset())
                    .setMaxResults(pageable.size())
                    .getResultList();

            return new Page<>(content, total, pageable.page(), pageable.size());
        });
    }
}
