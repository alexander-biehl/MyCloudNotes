package com.alexbiehl.mycloudnotes.integration;

import com.alexbiehl.mycloudnotes.utils.TestPostgresContainer;
import com.alexbiehl.mycloudnotes.model.Note;
import com.alexbiehl.mycloudnotes.model.User;
import com.alexbiehl.mycloudnotes.repository.NotesRepository;
import com.alexbiehl.mycloudnotes.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class SpringBootNotesIntegrationTest {

    @Container
    public static PostgreSQLContainer<TestPostgresContainer> postgreSQLContainer = TestPostgresContainer.getInstance();

    @Autowired
    private NotesRepository notesRepository;

    @Autowired
    private UserRepository userRepository;

    @SuppressWarnings("null")
    @Test
    @Transactional
    public void givenNotesRepository_whenSaveAndRetrieveEntity_thenOK() {
        UUID testUserId = UUID.randomUUID();
        UUID testNoteId = UUID.randomUUID();

        User testUser = userRepository.saveAndFlush(new User(testUserId, "test", "password", true));
        Note testNote = notesRepository.saveAndFlush(new Note(testNoteId, testUser, "title", "content"));

        Note foundNote = notesRepository.findById(testNote.getId()).get();
        assertNotNull(foundNote);
        assertEquals(testNote.getTitle(), foundNote.getTitle());
        assertEquals(testNote.getContent(), foundNote.getContent());
    }
}
