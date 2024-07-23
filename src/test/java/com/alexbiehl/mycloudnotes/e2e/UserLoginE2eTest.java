package com.alexbiehl.mycloudnotes.e2e;

import com.alexbiehl.mycloudnotes.MycloudnotesApplication;
import com.alexbiehl.mycloudnotes.api.API;
import com.alexbiehl.mycloudnotes.comms.TokenRefreshRequest;
import com.alexbiehl.mycloudnotes.comms.exception.TokenRefreshException;
import com.alexbiehl.mycloudnotes.components.JwtUtil;
import com.alexbiehl.mycloudnotes.components.SecurityConfiguration;
import com.alexbiehl.mycloudnotes.dto.UserDTO;
import com.alexbiehl.mycloudnotes.dto.UserLoginDTO;
import com.alexbiehl.mycloudnotes.model.RefreshToken;
import com.alexbiehl.mycloudnotes.model.User;
import com.alexbiehl.mycloudnotes.repository.RefreshTokenRepository;
import com.alexbiehl.mycloudnotes.repository.UserRepository;
import com.alexbiehl.mycloudnotes.utils.TestConstants;
import com.alexbiehl.mycloudnotes.utils.TestPostgresContainer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static com.alexbiehl.mycloudnotes.utils.TestConstants.REFRESH_TOKEN_EXPIRY_MS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {MycloudnotesApplication.class})
@Testcontainers
@AutoConfigureMockMvc
@Import(SecurityConfiguration.class)
@Transactional
public class UserLoginE2eTest {

    @Container
    public static PostgreSQLContainer<TestPostgresContainer> postgreSQLContainer = TestPostgresContainer.getInstance();

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private JwtUtil jwtUtil;

    @Test
    public void testRegister() throws Exception {
        UserDTO userDTO = new UserDTO(true, "loginTest", UUID.randomUUID());
        mockMvc.perform(
                        post(API.USERS + API.REGISTER_USER)
                                // .with(csrf())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(userDTO)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value(userDTO.getUsername()));
    }

    @Test
    public void givenUser_testLogin_andOk() throws Exception {
        User testUser = userRepository.getReferenceById(TestConstants.TEST_USER_ID);
        UserLoginDTO userLoginDTO = new UserLoginDTO(testUser.getUsername(), TestConstants.PLAIN_TEXT_PASSWORD);

        mockMvc.perform(
                        post(API.AUTH + API.LOGIN_USER)
                                // .with(csrf())
                                .content(objectMapper.writeValueAsString(userLoginDTO))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    public void givenUserAndInvalidPassword_testLogin_andFail() throws Exception {
        User testUser = userRepository.getReferenceById(TestConstants.TEST_USER_ID);
        UserLoginDTO userLoginDTO = new UserLoginDTO(testUser.getUsername(), "an invalid password");

        mockMvc.perform(
                        post(API.AUTH + API.LOGIN_USER)
                                // .with(csrf())
                                .content(objectMapper.writeValueAsString(userLoginDTO))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(result -> assertInstanceOf(UsernameNotFoundException.class, result.getResolvedException()))
                .andExpect(result -> assertEquals("Username or password did not match", result.getResolvedException().getMessage()));
    }

    @Test
    public void invalidUser_testLogin_andFail() throws Exception {
        UserLoginDTO invalidUser = new UserLoginDTO("invalid_User", "password");

        mockMvc.perform(
                        post(API.AUTH + API.LOGIN_USER)
                                // .with(csrf())
                                .content(objectMapper.writeValueAsString(invalidUser))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(result -> assertInstanceOf(UsernameNotFoundException.class, result.getResolvedException()))
                .andExpect(result -> assertEquals(String.format("User %s not found", invalidUser.getUsername()), result.getResolvedException().getMessage()));
    }

    @Test
    public void validUser_testRefreshToken_andPass() throws Exception {
        User testUser = userRepository.getReferenceById(TestConstants.TEST_USER_ID);

        RefreshToken refreshToken = new RefreshToken(testUser, UUID.randomUUID(), Instant.now().plusMillis(REFRESH_TOKEN_EXPIRY_MS));
        refreshToken = refreshTokenRepository.save(refreshToken);
        TokenRefreshRequest refreshRequest = new TokenRefreshRequest(refreshToken.getToken().toString());

        mockMvc.perform(
                        post(API.AUTH + API.REFRESH_TOKEN)
                                // .with(csrf())
                                .content(objectMapper.writeValueAsString(refreshRequest))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());

        // ensure the old token was deleted
        assertEquals(refreshTokenRepository.findByToken(refreshToken.getToken()), Optional.empty());
    }

    @Test
    public void invalidToken_testRefreshToken_andFail() throws Exception {
        User testUser = userRepository.getReferenceById(TestConstants.TEST_USER_ID);

        RefreshToken refreshToken = new RefreshToken(testUser, UUID.randomUUID(), Instant.now().plusMillis(REFRESH_TOKEN_EXPIRY_MS));
        TokenRefreshRequest refreshRequest = new TokenRefreshRequest(refreshToken.getToken().toString());

        mockMvc.perform(
                        post(API.AUTH + API.REFRESH_TOKEN)
                                // .with(csrf())
                                .content(objectMapper.writeValueAsString(refreshRequest))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(result -> assertInstanceOf(TokenRefreshException.class, result.getResolvedException()))
                .andExpect(result ->
                        assertEquals(
                                String.format("Failed for [%s]: %s", refreshToken.getToken().toString(), "Invalid Refresh Token"),
                                result.getResolvedException().getMessage()));
    }

    @Test
    public void expiredToken_testRefreshToken_andFail() throws Exception {
        User testUser = userRepository.getReferenceById(TestConstants.TEST_USER_ID);

        RefreshToken refreshToken = new RefreshToken(testUser, UUID.randomUUID(), Instant.now());
        refreshToken = refreshTokenRepository.save(refreshToken);
        TokenRefreshRequest refreshRequest = new TokenRefreshRequest(refreshToken.getToken().toString());
        final String tokenId = refreshToken.getToken().toString();
        mockMvc.perform(
                        post(API.AUTH + API.REFRESH_TOKEN)
                                // .with(csrf())
                                .content(objectMapper.writeValueAsString(refreshRequest))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(result -> assertInstanceOf(TokenRefreshException.class, result.getResolvedException()))
                .andExpect(result ->
                        assertEquals(
                                String.format("Failed for [%s]: %s", tokenId, "Refresh token was expired. Please sign in."),
                                result.getResolvedException().getMessage()));
    }
}
