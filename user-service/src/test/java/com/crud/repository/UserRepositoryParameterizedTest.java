package com.crud.repository;

import com.crud.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
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
class UserRepositoryParameterizedTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        createUser("Alice", "alice@example.com", 25);
        createUser("Bob", "bob@test.com", 30);
        createUser("Charlie", "charlie@example.com", 35);
        createUser("Diana", "diana@test.com", 28);
        entityManager.flush();
    }

    @SuppressWarnings("UnusedReturnValue")
    private User createUser(String name, String email, int age) {
        User user = User.builder()
                .name(name)
                .email(email)
                .age(age)
                .build();
        return entityManager.persist(user);
    }

    @ParameterizedTest
    @CsvSource({
            "alice@example.com, true",
            "bob@test.com, true",
            "nonexistent@email.com, false",
            "test@test.com, false"
    })
    void findByEmail_ShouldReflectExistence(String email, boolean expected) {
        boolean actual = userRepository.findByEmail(email).isPresent();

        assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {"example", "test", "alice", "bob"})
    void findByEmailContaining_ShouldFindMatchingUsers(String pattern) {
        Page<User> users = userRepository.findByEmailContaining(pattern, PageRequest.of(0, 10));

        assertThat(users.getContent())
                .isNotEmpty()
                .allMatch(user -> user.getEmail().contains(pattern));
    }

    @ParameterizedTest
    @ValueSource(strings = {"xyz", "nomatch", "unknown"})
    void findByEmailContaining_WithNonMatchingPattern_ShouldReturnEmpty(String pattern) {
        Page<User> users = userRepository.findByEmailContaining(pattern, PageRequest.of(0, 10));

        assertThat(users.getContent()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
            "alice@example.com, Alice",
            "bob@test.com, Bob",
            "charlie@example.com, Charlie"
    })
    void findByEmail_ShouldReturnCorrectUser(String email, String expectedName) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        assertThat(userOpt)
                .isPresent()
                .hasValueSatisfying(user -> assertThat(user.getName()).isEqualTo(expectedName));
    }
}
