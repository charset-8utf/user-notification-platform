package com.crud.repository;

import com.crud.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByEmail_WhenExists_ShouldReturnUser() {
        User user = User.builder()
                .name("John Doe")
                .email("john.doe@test.com")
                .age(30)
                .build();
        entityManager.persist(user);
        entityManager.flush();

        Optional<User> found = userRepository.findByEmail("john.doe@test.com");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("John Doe");
        assertThat(found.get().getAge()).isEqualTo(30);
    }

    @Test
    void findByEmail_WhenNotExists_ShouldReturnEmpty() {
        Optional<User> found = userRepository.findByEmail("nonexistent@test.com");

        assertThat(found).isEmpty();
    }

    @Test
    void save_ShouldAssignIdAndVersion() {
        User user = User.builder()
                .name("Jane Doe")
                .email("jane.doe@test.com")
                .age(25)
                .build();

        User saved = userRepository.save(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getVersion()).isZero();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void findById_WhenExists_ShouldReturnUser() {
        User user = User.builder()
                .name("Test User")
                .email("test@test.com")
                .age(35)
                .build();
        User persisted = entityManager.persist(user);
        entityManager.flush();

        Optional<User> found = userRepository.findById(persisted.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test User");
    }

    @Test
    void deleteById_ShouldRemoveUser() {
        User user = User.builder()
                .name("To Delete")
                .email("delete@test.com")
                .age(40)
                .build();
        User persisted = entityManager.persist(user);
        entityManager.flush();

        userRepository.deleteById(persisted.getId());
        entityManager.flush();

        Optional<User> found = userRepository.findById(persisted.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void updateUser_ShouldUpdateFieldsAndIncrementVersion() {
        User user = User.builder()
                .name("Original")
                .email("original@test.com")
                .age(20)
                .build();
        User saved = userRepository.save(user);
        Long originalVersion = saved.getVersion();

        saved.setName("Updated");
        saved.setAge(25);
        User updated = userRepository.saveAndFlush(saved);

        assertThat(updated.getName()).isEqualTo("Updated");
        assertThat(updated.getAge()).isEqualTo(25);
        assertThat(updated.getVersion()).isGreaterThan(originalVersion);
    }

    @Test
    void findAll_WithPagination_ShouldReturnPage() {
        for (int i = 1; i <= 5; i++) {
            User user = User.builder()
                    .name("User " + i)
                    .email("user" + i + "@test.com")
                    .age(20 + i)
                    .build();
            entityManager.persist(user);
        }
        entityManager.flush();

        Page<User> page = userRepository.findAll(PageRequest.of(0, 3));

        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(5);
        assertThat(page.getTotalPages()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void findByEmailContaining_ShouldReturnMatchingUsers() {
        User user1 = User.builder().name("John").email("john@gmail.com").age(30).build();
        User user2 = User.builder().name("Jane").email("jane@yahoo.com").age(25).build();
        User user3 = User.builder().name("Bob").email("bob@gmail.com").age(35).build();
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(user3);
        entityManager.flush();

        Page<User> gmailUsers = userRepository.findByEmailContaining("gmail", PageRequest.of(0, 10));

        assertThat(gmailUsers.getContent()).hasSize(2);
        assertThat(gmailUsers.getContent()).extracting(User::getEmail)
                .allMatch(email -> email.contains("gmail"));
    }
}
