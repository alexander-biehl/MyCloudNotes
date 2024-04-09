package com.alexbiehl.mycloudnotes.integration.repository;

import com.alexbiehl.mycloudnotes.model.Note;
import com.alexbiehl.mycloudnotes.model.User;
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

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


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

    @Test
    public void givenNotesRepository_whenRetrieve_thenNotOk() throws Exception{
        assertTrue(notesRepository.findById(UUID.randomUUID()).isEmpty());
    }

    @Test
    public void givenNotes_whenUpdate_thenOk() {
        Note note = notesRepository.findById(TestConstants.TEST_NOTE_ID).get();
        assertNotNull(note);
        assertEquals(note.getTitle(), "title");
        assertEquals(note.getContent(), "content");

        note.setContent("new content");
        notesRepository.saveAndFlush(note);
        Note updatedNote = notesRepository.getReferenceById(TestConstants.TEST_NOTE_ID);
        assertEquals(updatedNote.getContent(), "new content");
    }

    @Test
    public void givenNotes_whenDelete_thenOk() {
        notesRepository.deleteById(TestConstants.TEST_NOTE_ID);
        List<Note> notes = notesRepository.findAll();
        assertEquals(notes.size(), 1);
    }

    @Test
    public void givenNotes_whenCreate_thenOk() {
        final User testUser = userRepository.getReferenceById(TestConstants.TEST_USER_ID);
        assertNotNull(testUser, "Test User cannot be null");
        final Note newNote = new Note(testUser, "new title", "new content");
        // assertEquals(2, notesRepository.findAll().size(), "Should only contain 2 notes at test start");
        final Note savedNote = notesRepository.saveAndFlush(newNote);
        List<Note> foundNotes = notesRepository.findAll();

        assertEquals(3, foundNotes.size(), "Size should be 3 after creating new note");
        assertTrue(foundNotes.stream().anyMatch(note -> (TestConstants.TEST_NOTE_ID.equals(note.getId()))), "List of notes should contain the existing note");
        assertTrue(foundNotes.stream().anyMatch(note -> (savedNote.getId().equals(note.getId()))), "List of notes should contain the new note");

        final Note foundNote = notesRepository.getReferenceById(savedNote.getId());
        assertEquals(savedNote.getId(), foundNote.getId());
        assertEquals(newNote.getUser(), foundNote.getUser());
        assertEquals(newNote.getTitle(), foundNote.getTitle());
        assertEquals(newNote.getContent(), foundNote.getContent());
    }
}
