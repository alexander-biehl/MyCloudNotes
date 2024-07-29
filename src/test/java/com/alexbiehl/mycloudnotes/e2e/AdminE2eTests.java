package com.alexbiehl.mycloudnotes.e2e;

import com.alexbiehl.mycloudnotes.api.API;
import com.alexbiehl.mycloudnotes.components.JwtUtil;
import com.alexbiehl.mycloudnotes.components.SecurityConfiguration;
import com.alexbiehl.mycloudnotes.model.User;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@EnableAutoConfiguration
@Import(SecurityConfiguration.class)
@Transactional
public class AdminE2eTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminE2eTests.class);

    @Container
    public static PostgreSQLContainer<TestPostgresContainer> container = TestPostgresContainer.getInstance();

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private TestRestTemplate restTemplate;
    @LocalServerPort
    private int port;

    @Test
    public void givenAdmin_test_andOk() throws Exception {
        User testAdmin = userRepository.getReferenceById(TestConstants.TEST_ADMIN_ID);
        String authToken = TestUtils.getBearerToken(jwtUtil.createToken(testAdmin));

        ResponseEntity<Boolean> response = this.restTemplate.exchange(
                RequestEntity.get(TestUtils.uri(this.restTemplate, API.ADMIN))
                        .headers(TestUtils.headers("http://localhost/admin", authToken))
                        .build(),
                Boolean.class
        );

        LOGGER.info("Response: {}", response.toString());

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void givenUser_test_andFail() throws Exception {
        User testUser = userRepository.getReferenceById(TestConstants.TEST_USER_ID);
        String authToken = TestUtils.getBearerToken(jwtUtil.createToken(testUser));

        ResponseEntity<Boolean> response = this.restTemplate.exchange(
                RequestEntity.get(TestUtils.uri(this.restTemplate, API.ADMIN))
                        .headers(TestUtils.headers("http://localhost/admin", authToken))
                        .build(),
                Boolean.class
        );

        LOGGER.info("Response: {}", response.toString());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
}
