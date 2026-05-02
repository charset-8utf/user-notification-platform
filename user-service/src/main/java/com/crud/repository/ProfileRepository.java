package com.crud.repository;

import com.crud.dto.Page;
import com.crud.dto.Pageable;
import com.crud.entity.Profile;
import java.util.Optional;

/**
 * Репозиторий профилей.
 */
public interface ProfileRepository {

    Profile save(Profile profile);
    Optional<Profile> findById(Long id);
    Page<Profile> findAll(Pageable pageable);
    Profile update(Profile profile);
    void deleteById(Long id);
    Optional<Profile> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}
