package com.crud.repository;

import com.crud.entity.Profile;
import com.crud.entity.User;
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
class ProfileRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProfileRepository profileRepository;

    @Test
    void findByUserId_WhenExists_ShouldReturnProfile() {
        User user = User.builder().name("Test User").email("test@test.com").age(30).build();
        User persistedUser = entityManager.persist(user);
        
        Profile profile = Profile.builder()
                .phone("+1234567890")
                .address("Test Address")
                .user(persistedUser)
                .build();
        entityManager.persist(profile);
        entityManager.flush();

        Optional<Profile> found = profileRepository.findByUserId(persistedUser.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getPhone()).isEqualTo("+1234567890");
        assertThat(found.get().getAddress()).isEqualTo("Test Address");
    }

    @Test
    void findByUserId_WhenNotExists_ShouldReturnEmpty() {
        Optional<Profile> found = profileRepository.findByUserId(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void save_ShouldAssignIdAndTimestamps() {
        User user = User.builder().name("User").email("user@test.com").age(30).build();
        User persistedUser = entityManager.persist(user);
        
        Profile profile = Profile.builder()
                .phone("+999888777")
                .address("New Address")
                .user(persistedUser)
                .build();

        Profile saved = profileRepository.save(profile);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void deleteByUserId_ShouldRemoveProfile() {
        User user = User.builder().name("User").email("user@test.com").age(30).build();
        User persistedUser = entityManager.persist(user);
        
        Profile profile = Profile.builder()
                .phone("+111222333")
                .address("Delete Me")
                .user(persistedUser)
                .build();
        entityManager.persist(profile);
        entityManager.flush();

        profileRepository.deleteByUserId(persistedUser.getId());
        entityManager.flush();

        Optional<Profile> found = profileRepository.findByUserId(persistedUser.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void updateProfile_ShouldUpdateFields() {
        User user = User.builder().name("User").email("user@test.com").age(30).build();
        User persistedUser = entityManager.persist(user);
        
        Profile profile = Profile.builder()
                .phone("+000000000")
                .address("Old Address")
                .user(persistedUser)
                .build();
        Profile saved = entityManager.persist(profile);
        entityManager.flush();

        saved.setPhone("+1234567890");
        saved.setAddress("Updated Address");
        Profile updated = profileRepository.save(saved);

        assertThat(updated.getPhone()).isEqualTo("+1234567890");
        assertThat(updated.getAddress()).isEqualTo("Updated Address");
    }
}
