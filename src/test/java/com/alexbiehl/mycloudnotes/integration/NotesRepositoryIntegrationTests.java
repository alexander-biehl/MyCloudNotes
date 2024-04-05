package com.alexbiehl.mycloudnotes.integration;

import com.alexbiehl.mycloudnotes.model.Note;
import com.alexbiehl.mycloudnotes.repository.NotesRepository;
import com.alexbiehl.mycloudnotes.repository.UserRepository;
import com.alexbiehl.mycloudnotes.utils.TestConstants;
import com.alexbiehl.mycloudnotes.utils.TestPostgresContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


// @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class NotesRepositoryIntegrationTests {

    @Container
    public static PostgreSQLContainer<TestPostgresContainer> postgreSQLContainer = TestPostgresContainer.getInstance();

    @Autowired
    private NotesRepository notesRepository;

    @Autowired
    private UserRepository userRepository;


    @Test
    public void givenNotesRepository_whenRetrieveEntity_thenOK() {
        Note note = notesRepository.findById(TestConstants.TEST_NOTE_ID).get();
        assertNotNull(note);
        assertEquals(note.getTitle(), "title");
        assertEquals(note.getContent(), "content");
    }


}
