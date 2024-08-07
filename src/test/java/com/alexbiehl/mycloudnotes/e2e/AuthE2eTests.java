package com.alexbiehl.mycloudnotes.e2e;

import com.alexbiehl.mycloudnotes.api.API;
import com.alexbiehl.mycloudnotes.comms.JwtResponse;
import com.alexbiehl.mycloudnotes.comms.LoginRequest;
import com.alexbiehl.mycloudnotes.comms.TokenRefreshRequest;
import com.alexbiehl.mycloudnotes.comms.UserRegisterRequest;
import com.alexbiehl.mycloudnotes.components.JwtUtil;
import com.alexbiehl.mycloudnotes.components.SecurityConfiguration;
import com.alexbiehl.mycloudnotes.controller.advice.ErrorMessage;
import com.alexbiehl.mycloudnotes.dto.UserDTO;
import com.alexbiehl.mycloudnotes.dto.UserLoginDTO;
import com.alexbiehl.mycloudnotes.model.RefreshToken;
import com.alexbiehl.mycloudnotes.model.User;
import com.alexbiehl.mycloudnotes.repository.RefreshTokenRepository;
import com.alexbiehl.mycloudnotes.repository.RoleRepository;
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
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@EnableAutoConfiguration
@Import(SecurityConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
public class AuthE2eTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthE2eTests.class);

    @Container
    public static PostgreSQLContainer<TestPostgresContainer> postgreSQLContainer = TestPostgresContainer.getInstance();

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private TestRestTemplate restTemplate;
    @LocalServerPort
    private int port;

    @Test
    public void testRegister() throws Exception {
        User testUser = new User();
        testUser.setUsername("testUser");
        testUser.setPassword("password");
        UserRegisterRequest registerRequest = new UserRegisterRequest(testUser.getUsername(), testUser.getPassword());

        HttpHeaders headers = TestUtils.headers("http://localhost/users/register");

        HttpEntity<UserRegisterRequest> httpEntity = new HttpEntity<>(registerRequest, headers);

        ResponseEntity<String> response = this.restTemplate.postForEntity(
                TestUtils.uri(this.restTemplate, String.format("%s%s", API.USERS, API.REGISTER_USER)),
                httpEntity,
                String.class);

        LOGGER.info("Register response: {}", response);

        assertEquals(response.getStatusCode(), HttpStatus.CREATED);
        assertEquals("http://localhost/users/register", response.getHeaders().getAccessControlAllowOrigin());

        UserDTO registeredUser = objectMapper.readValue(response.getBody(), UserDTO.class);
        assertEquals(registeredUser.getUsername(), testUser.getUsername());
        assertNotNull(registeredUser.getId());

        LOGGER.info("Returned user: {}", registeredUser);
    }

    @Test
    public void givenUser_testLogin_andOk() throws Exception {
        User testUser = userRepository.getReferenceById(TestConstants.TEST_ADMIN_ID);
        UserLoginDTO loginDTO = new UserLoginDTO(testUser.getUsername(), TestConstants.PLAIN_TEXT_PASSWORD);

        HttpHeaders headers = TestUtils.headers("http://localhost/auth/login");
        HttpEntity<UserLoginDTO> entity = new HttpEntity<>(loginDTO, headers);

        ResponseEntity<String> response = this.restTemplate.postForEntity(
                TestUtils.uri(this.restTemplate, "/auth/login"),
                entity,
                String.class
        );

        LOGGER.info("Login Response: {}", response);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("http://localhost/auth/login", response.getHeaders().getAccessControlAllowOrigin());

        JwtResponse jwt = objectMapper.readValue(response.getBody(), JwtResponse.class);
        assertNotNull(jwt.getAccessToken());
        assertNotNull(jwt.getRefreshToken());
    }

    @Test
    public void givenUserAndInvalidPassword_testLogin_andFail() throws Exception {
        User testUser = userRepository.getReferenceById(TestConstants.TEST_USER_ID);
        UserLoginDTO loginDTO = new UserLoginDTO(testUser.getUsername(), "invalid password");

        HttpHeaders headers = TestUtils.headers("http://localhost/auth/login");
        HttpEntity<UserLoginDTO> entity = new HttpEntity<>(loginDTO, headers);

        ResponseEntity<String> response = this.restTemplate.postForEntity(
                TestUtils.uri(this.restTemplate, "/auth/login"),
                entity,
                String.class
        );

        LOGGER.info("Login Response: {}", response);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("http://localhost/auth/login", response.getHeaders().getAccessControlAllowOrigin());

        ErrorMessage ex = objectMapper.readValue(response.getBody(), ErrorMessage.class);
        assertEquals("Username or password did not match", ex.getMessage());
    }

    @Test
    public void invalidUser_testLogin_andFail() throws Exception {
        User testUser = new User();
        testUser.setUsername("invalidUser");
        UserLoginDTO loginDTO = new UserLoginDTO(testUser.getUsername(), "invalid password");

        HttpHeaders headers = TestUtils.headers("http://localhost/auth/login");
        HttpEntity<UserLoginDTO> entity = new HttpEntity<>(loginDTO, headers);

        ResponseEntity<String> response = this.restTemplate.postForEntity(
                TestUtils.uri(this.restTemplate, "/auth/login"),
                entity,
                String.class
        );

        LOGGER.info("Login Response: {}", response);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("http://localhost/auth/login", response.getHeaders().getAccessControlAllowOrigin());

        ErrorMessage ex = objectMapper.readValue(response.getBody(), ErrorMessage.class);
        assertEquals(String.format("User %s not found", testUser.getUsername()), ex.getMessage());
    }

    @Test
    public void validUser_testRefreshToken_andPass() throws Exception {
        RefreshToken refreshToken = refreshTokenRepository.getReferenceById(TestConstants.TEST_REFRESH_TOKEN_ID);
        TokenRefreshRequest refreshRequest = new TokenRefreshRequest(refreshToken.getToken().toString());

        List<RefreshToken> tokens = refreshTokenRepository.findAll();
        LOGGER.info("Saved tokens: {}", tokens);

        HttpHeaders headers = TestUtils.headers("http://localhost/auth/refreshtoken");
        HttpEntity<TokenRefreshRequest> entity = new HttpEntity<>(refreshRequest, headers);

        ResponseEntity<String> response = this.restTemplate.postForEntity(
                TestUtils.uri(this.restTemplate, String.format("%s%s", API.AUTH, API.REFRESH_TOKEN)),
                entity,
                String.class
        );

        LOGGER.info("Response was: {}", response);

        TestUtils.assertCodeAndOrigin(response, HttpStatus.OK, "http://localhost/auth/refreshtoken");

        JwtResponse jwt = objectMapper.readValue(response.getBody(), JwtResponse.class);
        assertNotNull(jwt.getRefreshToken());
        assertNotNull(jwt.getAccessToken());
        assertNotEquals(refreshToken.getToken().toString(), jwt.getRefreshToken());
        // ensure the old token was deleted
        assertFalse(refreshTokenRepository.existsById(refreshToken.getId()));
    }

    @Test
    public void invalidToken_testRefreshToken_andFail() throws Exception {
        UUID invalidToken = UUID.randomUUID();
        TokenRefreshRequest refreshRequest = new TokenRefreshRequest(invalidToken.toString());

        HttpHeaders headers = TestUtils.headers("http://localhost/auth/refreshtoken");
        HttpEntity<TokenRefreshRequest> entity = new HttpEntity<>(refreshRequest, headers);

        ResponseEntity<String> response = this.restTemplate.postForEntity(
                TestUtils.uri(this.restTemplate, String.format("%s%s", API.AUTH, API.REFRESH_TOKEN)),
                entity,
                String.class
        );

        TestUtils.assertCodeAndOrigin(response, HttpStatus.FORBIDDEN, "http://localhost/auth/refreshtoken");

        ErrorMessage error = objectMapper.readValue(response.getBody(), ErrorMessage.class);
        assertEquals(
                String.format("Failed for [%s]: Invalid Refresh Token", invalidToken),
                error.getMessage()
        );
    }

    @Test
    public void expiredToken_testRefreshToken_andFail() throws Exception {
        RefreshToken expiredToken = refreshTokenRepository.getReferenceById(TestConstants.TEST_EXPIRED_REFRESH_TOKEN_ID);
        TokenRefreshRequest refreshRequest = new TokenRefreshRequest(expiredToken.getToken().toString());
        final String tokenId = expiredToken.getToken().toString();

        HttpHeaders headers = TestUtils.headers("http://localhost/auth/refreshtoken");
        HttpEntity<TokenRefreshRequest> entity = new HttpEntity<>(refreshRequest, headers);

        ResponseEntity<String> response = this.restTemplate.postForEntity(
                TestUtils.uri(this.restTemplate, String.format("%s%s", API.AUTH, API.REFRESH_TOKEN)),
                entity,
                String.class
        );

        TestUtils.assertCodeAndOrigin(response, HttpStatus.FORBIDDEN, "http://localhost/auth/refreshtoken");

        ErrorMessage error = objectMapper.readValue(response.getBody(), ErrorMessage.class);
        assertEquals(
                String.format(
                        "Failed for [%s]: Refresh token has expired. Please sign in.",
                        expiredToken.getToken().toString()
                ),
                error.getMessage()
        );
    }

    @Test
    public void invalidOrigin_register_andFail() throws Exception {
        User testUser = userRepository.getReferenceById(TestConstants.TEST_USER_ID);
        UserRegisterRequest registerRequest = new UserRegisterRequest(testUser.getUsername(), TestConstants.PLAIN_TEXT_PASSWORD);

        HttpHeaders headers = TestUtils.headers("http://invalidsite.com");

        HttpEntity<UserRegisterRequest> entity = new HttpEntity<>(registerRequest, headers);

        ResponseEntity<String> response = this.restTemplate.postForEntity(
                TestUtils.uri(this.restTemplate, String.format("%s%s", API.AUTH, API.REGISTER_USER)),
                entity,
                String.class
        );

        LOGGER.info("Response: {}", response);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void noOrigin_register_andFail() throws Exception {
        User testUser = userRepository.getReferenceById(TestConstants.TEST_USER_ID);
        UserRegisterRequest registerRequest = new UserRegisterRequest(testUser.getUsername(), TestConstants.PLAIN_TEXT_PASSWORD);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<UserRegisterRequest> entity = new HttpEntity<>(registerRequest, headers);

        ResponseEntity<String> response = this.restTemplate.postForEntity(
                TestUtils.uri(this.restTemplate, String.format("%s%s", API.AUTH, API.REGISTER_USER)),
                entity,
                String.class
        );

        LOGGER.info("Response: {}", response);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void invalidOrigin_login_andFail() throws Exception {
        User testUser = userRepository.getReferenceById(TestConstants.TEST_USER_ID);
        LoginRequest loginRequest = new LoginRequest(testUser.getUsername(), TestConstants.PLAIN_TEXT_PASSWORD);

        HttpHeaders headers = TestUtils.headers("http://invalidsite.com");

        HttpEntity<LoginRequest> entity = new HttpEntity<>(loginRequest, headers);

        ResponseEntity<String> response = this.restTemplate.postForEntity(
                TestUtils.uri(this.restTemplate, String.format("%s%s", API.AUTH, API.LOGIN_USER)),
                entity,
                String.class
        );

        LOGGER.info("Response: {}", response);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void noOrigin_login_andFail() throws Exception {
        User testUser = userRepository.getReferenceById(TestConstants.TEST_USER_ID);
        LoginRequest loginRequest = new LoginRequest(testUser.getUsername(), TestConstants.PLAIN_TEXT_PASSWORD);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<LoginRequest> entity = new HttpEntity<>(loginRequest, headers);

        ResponseEntity<String> response = this.restTemplate.postForEntity(
                TestUtils.uri(this.restTemplate, String.format("%s%s", API.AUTH, API.REGISTER_USER)),
                entity,
                String.class
        );

        LOGGER.info("Response: {}", response);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void givenUser_logout_andOk() throws Exception {
        User testUser = userRepository.getReferenceById(TestConstants.TEST_USER_ID);

        HttpHeaders headers = TestUtils.headers(
                "http://localhost/logout",
                TestUtils.getBearerToken(
                        jwtUtil.createToken(testUser)
                )
        );
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<String> response = this.restTemplate.postForEntity(
                TestUtils.uri(this.restTemplate, API.AUTH + API.LOGOUT_USER),
                entity,
                String.class
        );

        LOGGER.info("Response: {}", response.toString());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("http://localhost/logout", response.getHeaders().getAccessControlAllowOrigin());
        assertFalse(refreshTokenRepository.existsById(TestConstants.TEST_REFRESH_TOKEN_ID));
    }

    @Test
    public void noAuth_logout_andFail() throws Exception {
        HttpHeaders headers = TestUtils.headers("http://localhost/logout");
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<String> response = this.restTemplate.postForEntity(
                TestUtils.uri(this.restTemplate, API.AUTH + API.LOGOUT_USER),
                entity,
                String.class
        );

        LOGGER.info("Response: {}", response.toString());

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void invalidUser_logout_andFail() throws Exception {
        User invalidUser = new User(UUID.randomUUID(), "testUser", "password", true);
        invalidUser.setRoles(Set.of(roleRepository.getReferenceById(TestConstants.USER_ROLE_ID)));

        HttpHeaders headers = TestUtils.headers(
                "http://localhost/logout",
                TestUtils.getBearerToken(jwtUtil.createToken(invalidUser))
        );
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<String> response = this.restTemplate.postForEntity(
                TestUtils.uri(this.restTemplate, API.AUTH + API.LOGOUT_USER),
                entity,
                String.class
        );

        LOGGER.info("Response: {}", response.toString());

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
