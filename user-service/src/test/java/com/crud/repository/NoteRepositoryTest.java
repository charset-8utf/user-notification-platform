package com.crud.repository;

import com.crud.entity.Note;
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
class NoteRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private NoteRepository noteRepository;

    @Test
    void findById_WhenExists_ShouldReturnNote() {
        User user = User.builder().name("Test User").email("test@test.com").age(30).build();
        User persistedUser = entityManager.persist(user);
        
        Note note = Note.builder().content("Test note content").user(persistedUser).build();
        Note persistedNote = entityManager.persist(note);
        entityManager.flush();

        Optional<Note> found = noteRepository.findById(persistedNote.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getContent()).isEqualTo("Test note content");
        assertThat(found.get().getUser().getId()).isEqualTo(persistedUser.getId());
    }

    @Test
    void findByUserId_ShouldReturnNotesForUser() {
        User user = User.builder().name("Note Owner").email("owner@test.com").age(25).build();
        User persistedUser = entityManager.persist(user);
        
        Note note1 = Note.builder().content("Note 1").user(persistedUser).build();
        Note note2 = Note.builder().content("Note 2").user(persistedUser).build();
        entityManager.persist(note1);
        entityManager.persist(note2);
        entityManager.flush();

        Page<Note> notes = noteRepository.findByUserId(persistedUser.getId(), PageRequest.of(0, 10));

        assertThat(notes.getContent()).hasSize(2);
        assertThat(notes.getContent()).extracting(Note::getContent)
                .containsExactlyInAnyOrder("Note 1", "Note 2");
    }

    @Test
    void save_ShouldAssignIdAndTimestamps() {
        User user = User.builder().name("User").email("user@test.com").age(30).build();
        User persistedUser = entityManager.persist(user);
        
        Note note = Note.builder().content("New note").user(persistedUser).build();

        Note saved = noteRepository.save(note);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void deleteById_ShouldRemoveNote() {
        User user = User.builder().name("User").email("user@test.com").age(30).build();
        User persistedUser = entityManager.persist(user);
        
        Note note = Note.builder().content("To delete").user(persistedUser).build();
        Note persisted = entityManager.persist(note);
        entityManager.flush();

        noteRepository.deleteById(persisted.getId());
        entityManager.flush();

        Optional<Note> found = noteRepository.findById(persisted.getId());
        assertThat(found).isEmpty();
    }
}
