package com.alexbiehl.mycloudnotes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.ClassRule;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import com.alexbiehl.mycloudnotes.model.Note;
import com.alexbiehl.mycloudnotes.repository.NotesRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycloudnotesApplication.class)
@ActiveProfiles({ "tc", "tc-auto" })
public class SpringBootNotesIntegrationTest {

    @ClassRule
    @Container
    public static PostgreSQLContainer<TestPostgresContainer> postgreSQLContainer = TestPostgresContainer.getInstance();

    @Autowired
    private NotesRepository notesRepository;

    @Test
    @Transactional
    public void givenNotesRepository_whenSaveAndRetrieveEntity_thenOK() {
        Note note = notesRepository.save(new Note(UUID.randomUUID(), "Note", "Hello, World!"));
        Note foundNote = notesRepository.findById(note.getId()).get();
        assertNotNull(foundNote);
        assertEquals(note.getTitle(), foundNote.getTitle());
        assertEquals(note.getContent(), foundNote.getContent());
    }
}
