package com.alexbiehl.mycloudnotes.e2e;

import com.alexbiehl.mycloudnotes.api.API;
import com.alexbiehl.mycloudnotes.components.JwtUtil;
import com.alexbiehl.mycloudnotes.components.SecurityConfiguration;
import com.alexbiehl.mycloudnotes.dto.NoteDTO;
import com.alexbiehl.mycloudnotes.model.User;
import com.alexbiehl.mycloudnotes.repository.UserRepository;
import com.alexbiehl.mycloudnotes.utils.TestConstants;
import com.alexbiehl.mycloudnotes.utils.TestPostgresContainer;
import com.alexbiehl.mycloudnotes.utils.TestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@EnableAutoConfiguration
@Import(SecurityConfiguration.class)
@Transactional
public class NotesE2eTest {

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
    private JwtUtil jwtUtil;

    // searching

    @Test
    public void givenUser_getNotes_andOk() {
        User testUser = userRepository.getReferenceById(TestConstants.TEST_USER_ID);
        String jwt = String.format("%s %s", JwtUtil.TOKEN_PREFIX, jwtUtil.createToken(testUser));

        ResponseEntity<NoteDTO[]> response = this.restTemplate.exchange(
                RequestEntity.get(
                                TestUtils.uri(this.restTemplate, API.NOTES))
                        .header(HttpHeaders.ORIGIN, "http://localhost:89998")
                        .header(HttpHeaders.AUTHORIZATION, jwt)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .build(),
                NoteDTO[].class
        );

        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertEquals("http://localhost:89998", response.getHeaders().getAccessControlAllowOrigin());
    }

    @Test
    public void givenUserNoOriginNoJwt_getNotes_andFail() {
        User testUser = userRepository.getReferenceById(TestConstants.TEST_USER_ID);

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
        String jwt = String.format("%s %s", JwtUtil.TOKEN_PREFIX, jwtUtil.createToken(testUser));

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
        User testUser = userRepository.getReferenceById(TestConstants.TEST_USER_ID);

        ResponseEntity<NoteDTO[]> response = this.restTemplate.exchange(
                RequestEntity.get(
                                TestUtils.uri(this.restTemplate, API.NOTES))
                        .header(HttpHeaders.ORIGIN, "http://localhost:88989")
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .build(),
                NoteDTO[].class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
    // creating


    // updating

    // deleting
}
