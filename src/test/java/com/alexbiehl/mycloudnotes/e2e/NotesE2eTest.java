package com.alexbiehl.mycloudnotes.e2e;

import com.alexbiehl.mycloudnotes.api.API;
import com.alexbiehl.mycloudnotes.components.JwtUtil;
import com.alexbiehl.mycloudnotes.components.SecurityConfiguration;
import com.alexbiehl.mycloudnotes.dto.NoteDTO;
import com.alexbiehl.mycloudnotes.model.Note;
import com.alexbiehl.mycloudnotes.model.User;
import com.alexbiehl.mycloudnotes.repository.NotesRepository;
import com.alexbiehl.mycloudnotes.repository.UserRepository;
import com.alexbiehl.mycloudnotes.utils.TestConstants;
import com.alexbiehl.mycloudnotes.utils.TestPostgresContainer;
import com.alexbiehl.mycloudnotes.utils.TestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@EnableAutoConfiguration
@Import(SecurityConfiguration.class)
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class NotesE2eTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotesE2eTest.class);

    @Container
    private static final PostgreSQLContainer<TestPostgresContainer> container = TestPostgresContainer.getInstance();

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotesRepository notesRepository;

    @Autowired
    private JwtUtil jwtUtil;


    @Test
    public void givenUser_getNotes_andOk() {
        User testUser = userRepository.getReferenceById(TestConstants.TEST_USER_ID);
        String jwt = TestUtils.getBearerToken(jwtUtil.createToken(testUser));

        List<Note> notes = notesRepository.findAll();
        LOGGER.info("Existing Notes: {}", notes);

        ResponseEntity<NoteDTO[]> response = this.restTemplate.exchange(
                RequestEntity.get(
                                TestUtils.uri(this.restTemplate, API.NOTES))
                        .headers(TestUtils.headers("http://localhost:89998", jwt))
                        .build(),
                NoteDTO[].class
        );

        LOGGER.info("Response: {}", response.toString());

        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertEquals("http://localhost:89998", response.getHeaders().getAccessControlAllowOrigin());
        assertEquals(1, response.getBody().length);

        NoteDTO note = response.getBody()[0];
        assertEquals("title", note.getTitle());
        assertEquals("content", note.getContent());
        assertEquals(testUser.getId(), note.getUserId());
    }

    @Test
    public void givenUserNoOriginNoJwt_getNotes_andFail() {
        ResponseEntity<NoteDTO[]> response = this.restTemplate.exchange(
                RequestEntity.get(
                                TestUtils.uri(this.restTemplate, API.NOTES))
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .build(),
                NoteDTO[].class
        );

        assertEquals(response.getStatusCode(), HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void givenUserNoOrigin_getNotes_andOk() {
        User testUser = userRepository.getReferenceById(TestConstants.TEST_USER_ID);
        String jwt = TestUtils.getBearerToken(jwtUtil.createToken(testUser));

        ResponseEntity<NoteDTO[]> response = this.restTemplate.exchange(
                RequestEntity.get(
                                TestUtils.uri(this.restTemplate, API.NOTES))
                        .header(HttpHeaders.AUTHORIZATION, jwt)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .build(),
                NoteDTO[].class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void givenUserNoJwt_getNotes_andFail() {
        ResponseEntity<String> response = this.restTemplate.exchange(
                RequestEntity.get(
                                TestUtils.uri(this.restTemplate, API.NOTES))
                        //.header(HttpHeaders.ORIGIN, "http://localhost:88989")
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .build(),
                String.class
        );

        LOGGER.info("response: {}", response.toString());

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void givenUser_getNoteById_andOK() throws Exception {
        User testUser = userRepository.getReferenceById(TestConstants.TEST_USER_ID);
        String authHeader = TestUtils.getBearerToken(jwtUtil.createToken(testUser));

        RequestEntity<NoteDTO> entity = new RequestEntity<>(
                TestUtils.headers("http://localhost/notes/", authHeader),
                HttpMethod.GET,
                TestUtils.uri(this.restTemplate, API.NOTES + "/" + TestConstants.TEST_NOTE_ID)
        );

        ResponseEntity<NoteDTO> response = this.restTemplate.exchange(
                entity,
                NoteDTO.class
        );

        LOGGER.info("Response: {}", response.toString());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(NoteDTO.class, response.getBody());
        assertEquals("title", response.getBody().getTitle());
        assertEquals("content", response.getBody().getContent());
    }

    // creating
    @Test
    public void givenUser_postNote_andOk() throws Exception {
        User testUser = userRepository.getReferenceById(TestConstants.TEST_USER_ID);
        String authToken = TestUtils.getBearerToken(jwtUtil.createToken(testUser));
        NoteDTO newNote = new NoteDTO(testUser.getId(), "new title", "new content");

        HttpHeaders headers = TestUtils.headers("http://localhost/notes", authToken);
        HttpEntity<NoteDTO> entity = new HttpEntity<>(newNote, headers);

        ResponseEntity<NoteDTO> response = this.restTemplate.postForEntity(
                TestUtils.uri(this.restTemplate, API.NOTES),
                entity,
                NoteDTO.class
        );

        LOGGER.info("Response: {}", response.toString());

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertInstanceOf(NoteDTO.class, response.getBody());
        assertEquals(newNote.getTitle(), response.getBody().getTitle());
        assertEquals(newNote.getContent(), response.getBody().getContent());
        assertEquals(testUser.getId(), response.getBody().getUserId());
        assertNotNull(response.getBody().getId());
    }

    // updating
    @Test
    public void givenUser_updateNote_andOk() throws Exception {
        User testUser = userRepository.getReferenceById(TestConstants.TEST_USER_ID);
        String authToken = TestUtils.getBearerToken(jwtUtil.createToken(testUser));
        Note note = notesRepository.getReferenceById(TestConstants.TEST_NOTE_ID);
        NoteDTO updateNote = NoteDTO.from(note);
        updateNote.setContent("new content");

        HttpHeaders headers = TestUtils.headers("http://localhost/notes", authToken);
        HttpEntity<NoteDTO> entity = new HttpEntity<>(updateNote, headers);

        ResponseEntity<NoteDTO> response = this.restTemplate.exchange(
                TestUtils.uri(this.restTemplate, API.NOTES + "/" + updateNote.getId().toString()),
                HttpMethod.PUT,
                entity,
                NoteDTO.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(NoteDTO.class, response.getBody());
        assertEquals(updateNote.getTitle(), response.getBody().getTitle());
        assertEquals(updateNote.getContent(), response.getBody().getContent());
        assertEquals(testUser.getId(), response.getBody().getUserId());
        assertNotNull(response.getBody().getId());
    }

    @Test
    public void givenUserAndNote_delete_andOk() throws Exception {
        User testUser = userRepository.getReferenceById(TestConstants.TEST_USER_ID);
        Note note = notesRepository.getReferenceById(TestConstants.TEST_NOTE_ID);
        String authToken = TestUtils.getBearerToken(jwtUtil.createToken(testUser));

        ResponseEntity<Void> response = this.restTemplate.exchange(
                TestUtils.uri(this.restTemplate, API.NOTES + "/" + note.getId().toString()),
                HttpMethod.DELETE,
                new HttpEntity<>(TestUtils.headers("http://localhost/notes/", authToken)),
                Void.class
        );

        LOGGER.info("response: {}", response.toString());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(notesRepository.findById(note.getId()), Optional.empty());
    }

    @Test
    public void givenInvalidUser_delete_andFail() throws Exception {
        User testUser2 = userRepository.getReferenceById(TestConstants.TEST_USER2_ID);
        Note note = notesRepository.getReferenceById(TestConstants.TEST_NOTE_ID);
        String authToken = TestUtils.getBearerToken(jwtUtil.createToken(testUser2));

        ResponseEntity<Void> response = this.restTemplate.exchange(
                TestUtils.uri(this.restTemplate, API.NOTES + "/" + note.getId().toString()),
                HttpMethod.DELETE,
                new HttpEntity<>(TestUtils.headers("http://localhost/notes/" + note.getId().toString(), authToken)),
                Void.class
        );

        LOGGER.info("Response: {}", response.toString());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void givenAdmin_delete_andOk() throws Exception {
        User testAdmin = userRepository.getReferenceById(TestConstants.TEST_ADMIN_ID);
        Note userNote = notesRepository.getReferenceById(TestConstants.TEST_NOTE_ID);
        String authToken = TestUtils.getBearerToken(jwtUtil.createToken(testAdmin));

        ResponseEntity<Void> response = this.restTemplate.exchange(
                TestUtils.uri(this.restTemplate, API.NOTES + "/" + userNote.getId().toString()),
                HttpMethod.DELETE,
                new HttpEntity<>(TestUtils.headers("http://localhost/notes/" + userNote.getId().toString(), authToken)),
                Void.class
        );

        LOGGER.info("Response: {}", response.toString());

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
