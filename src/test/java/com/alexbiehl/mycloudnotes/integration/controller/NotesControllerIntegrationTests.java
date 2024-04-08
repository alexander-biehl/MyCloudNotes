package com.alexbiehl.mycloudnotes.integration.controller;


import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

import com.alexbiehl.mycloudnotes.components.SecurityConfiguration;
import com.alexbiehl.mycloudnotes.model.User;
import com.alexbiehl.mycloudnotes.repository.UserRepository;
import com.alexbiehl.mycloudnotes.utils.TestConstants;
import com.alexbiehl.mycloudnotes.utils.TestPostgresContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

// @WebMvcTest(NotesController.class)
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

    @Test
    @WithUserDetails(value = "test_user", userDetailsServiceBeanName = "userDetailsService")
    public void givenUserAndNotes_confirmUserOnlySeesOwnNotes() throws Exception {
        final User testUser = userRepository.getReferenceById(TestConstants.TEST_USER_ID);
        mockMvc.perform(
                get("/notes")
                        .with(httpBasic("test_user", "password"))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].user.id").value(testUser.getId().toString()));
    }

    @Test
    public void confirmAuthRequired() throws Exception {
        mockMvc.perform(get("/notes")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}
