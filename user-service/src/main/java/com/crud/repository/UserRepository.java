package com.crud.repository;

import com.crud.dto.Page;
import com.crud.dto.Pageable;
import com.crud.entity.User;
import java.util.Optional;

/**
 * Репозиторий пользователей.
 */
public interface UserRepository {

    User save(User user);
    Optional<User> findById(Long id);
    Page<User> findAll(Pageable pageable);
    User update(User user);
    void deleteById(Long id);
    Optional<User> findByEmail(String email);
    Optional<User> findByIdWithLock(Long id);
}
