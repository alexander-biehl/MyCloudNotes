package com.alexbiehl.mycloudnotes.integration.controller;


import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

import com.alexbiehl.mycloudnotes.api.API;
import com.alexbiehl.mycloudnotes.components.SecurityConfiguration;
import com.alexbiehl.mycloudnotes.dto.NoteDTO;
import com.alexbiehl.mycloudnotes.model.User;
import com.alexbiehl.mycloudnotes.repository.UserRepository;
import com.alexbiehl.mycloudnotes.utils.TestConstants;
import com.alexbiehl.mycloudnotes.utils.TestPostgresContainer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


@Transactional
@SpringBootTest
@Import(SecurityConfiguration.class)
@AutoConfigureMockMvc
@Testcontainers
public class NotesControllerIntegrationTests {

    @Container
    public static PostgreSQLContainer<TestPostgresContainer> postgreSQLContainer = TestPostgresContainer.getInstance();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithUserDetails(value = "test_user", userDetailsServiceBeanName = "userDetailsService")
    public void givenUserAndNotes_confirmUserOnlySeesOwnNotes() throws Exception {
        final User testUser = userRepository.getReferenceById(TestConstants.TEST_USER_ID);
        mockMvc.perform(
                get(API.NOTES)
                        .with(httpBasic("test_user", "password"))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId").value(testUser.getId().toString()));
    }

    @Test
    public void confirmAuthRequired() throws Exception {
        mockMvc.perform(
                get(API.NOTES)
                    .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails(value = "test_admin", userDetailsServiceBeanName = "userDetailsService")
    public void givenAdminAndNotes_confirmAdminSeesAll() throws Exception {
        final User adminUser = userRepository.getReferenceById(TestConstants.TEST_ADMIN_ID);
        mockMvc.perform(
                get(API.NOTES)
                        .with(httpBasic("test_admin", "password"))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @WithUserDetails(value = "test_user2", userDetailsServiceBeanName = "userDetailsService")
    public void givenUserNoNotes_confirmEmptyList() throws Exception {
        mockMvc.perform(
                get(API.NOTES)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(httpBasic("test_user2", "password")))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithUserDetails(value = "test_user", userDetailsServiceBeanName = "userDetailsService")
    public void givenUser_createNote_andOk() throws Exception {
        User user = userRepository.getReferenceById(TestConstants.TEST_USER_ID);
        NoteDTO testNote = new NoteDTO("testPOST", "This is a POST Test");

        mockMvc.perform(
                post(API.NOTES)
                        // .with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testNote)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userId").value(user.getId().toString()));
    }

    @Test
    public void onPost_confirmAuthRequired() throws Exception {
        mockMvc.perform(
                post(API.NOTES)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}
