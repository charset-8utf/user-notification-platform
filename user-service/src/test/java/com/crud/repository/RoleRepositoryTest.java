package com.crud.repository;

import com.crud.entity.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class RoleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void findById_WhenExists_ShouldReturnRole() {
        Role role = Role.builder().name("TEST_ROLE").build();
        Role persisted = entityManager.persist(role);
        entityManager.flush();

        Optional<Role> found = roleRepository.findById(persisted.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("TEST_ROLE");
    }

    @Test
    void save_ShouldAssignId() {
        Role role = Role.builder().name("NEW_ROLE").build();

        Role saved = roleRepository.save(role);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("NEW_ROLE");
    }

    @Test
    void deleteById_ShouldRemoveRole() {
        Role role = Role.builder().name("TO_DELETE").build();
        Role persisted = entityManager.persist(role);
        entityManager.flush();

        roleRepository.deleteById(persisted.getId());
        entityManager.flush();

        Optional<Role> found = roleRepository.findById(persisted.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void updateRole_ShouldUpdateName() {
        Role role = Role.builder().name("OLD_NAME").build();
        Role saved = entityManager.persist(role);
        entityManager.flush();

        saved.setName("UPDATED_NAME");
        Role updated = roleRepository.save(saved);

        assertThat(updated.getName()).isEqualTo("UPDATED_NAME");
    }
}
