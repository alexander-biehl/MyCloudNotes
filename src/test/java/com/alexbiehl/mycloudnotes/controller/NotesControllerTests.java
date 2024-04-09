package com.alexbiehl.mycloudnotes.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.alexbiehl.mycloudnotes.service.UserService;
import com.alexbiehl.mycloudnotes.utils.TestUtils;
import com.alexbiehl.mycloudnotes.model.User;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.alexbiehl.mycloudnotes.dto.NoteDTO;
import com.alexbiehl.mycloudnotes.model.Note;
import com.alexbiehl.mycloudnotes.service.NotesService;
import com.fasterxml.jackson.databind.ObjectMapper;


// use @WebMvcTest so we are not loading the whole application context
@WebMvcTest(controllers = NotesController.class)
// use addFilters = False so security filters don't get loaded
@AutoConfigureMockMvc(addFilters = false)
public class NotesControllerTests {


        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean(name = "notesService")
        private NotesService notesService;

        @MockBean(name = "userService")
        private UserService userService;



        @Test
        @WithAnonymousUser
        public void withoutData_testGetNotesEndpoint() throws Exception {
                mockMvc.perform(
                                get("/notes")
                                                .accept(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[*]").isEmpty());
        }

        @SuppressWarnings("null")
        @Test
        @WithAnonymousUser
        public void withData_testGetNoteEndpoint() throws Exception {
                // Define sample data
                UUID id = UUID.randomUUID();
                User user = TestUtils.generateUser();
                Note note = new Note(id, user, "TestNote", "Test note content.");

                // Mock the Service behaviour
                Mockito.when(notesService.getNoteById(id)).thenReturn(note);

                // perform the test
                mockMvc.perform(
                                get("/notes/" + id.toString())
                                                .accept(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(note.getId().toString()))
                                .andExpect(jsonPath("$.userId").value(note.getUser().getId().toString()))
                                .andExpect(jsonPath("$.title").value(note.getTitle()))
                                .andExpect(jsonPath("$.content").value(note.getContent()));
        }

        @SuppressWarnings("null")
        @Test
        @WithAnonymousUser
        public void withoutData_testGetNoteEndpoint_andIsNotFound() throws Exception {
                UUID id = UUID.randomUUID();

                // mock the service behaviour
                Mockito.when(notesService.getNoteById(id)).thenReturn(null);

                mockMvc.perform(
                                get("/notes/" + id.toString())
                                                .accept(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isNotFound());
        }

        @SuppressWarnings("null")
        @Test
        @WithMockUser
        public void testPostNote_andOk() throws Exception {
                // set up test data
                User user = TestUtils.generateUser();
                NoteDTO testNote = new NoteDTO(null,"Test Note", "Note Content.");
                Note toReturnNote = Note.from(testNote);
                toReturnNote.setId(UUID.randomUUID());
                toReturnNote.setUser(user);
                // mock return values
                Mockito.when(userService.getUserByUsername(Mockito.anyString())).thenReturn(user);
                Mockito.when(notesService.save(Mockito.any(Note.class))).thenReturn(toReturnNote);

                // perform test
                mockMvc.perform(
                                post("/notes")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .accept(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(testNote)))
                                .andDo(print())
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").exists());
        }

        @Test
        @WithMockUser
        public void testPostNote_userNotFound() throws Exception {
                User user = TestUtils.generateUser();
                NoteDTO testNote = new NoteDTO(null, "Test Note", "Note Content.");

                Mockito.when(userService.getUserByUsername(Mockito.anyString())).thenReturn(null);

                mockMvc.perform(
                        post("/notes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testNote)))
                        .andDo(print())
                        .andExpect(status().isNotFound());
        }

        @SuppressWarnings("null")
        @Test
        @WithMockUser
        public void testNoteDoesntExists_PutNote_andOk() throws Exception {
                // set up test data
                UUID id = UUID.randomUUID();
                User user = TestUtils.generateUser();
                NoteDTO dto = new NoteDTO(id, "New Note", "Note Content.");
                Note returnNote = Note.from(dto);
                returnNote.setUser(user);
                returnNote.setId(id);

                // mock return values
                Mockito.when(userService.getUserByUsername(Mockito.anyString())).thenReturn(user);
                Mockito.when(notesService.exists(id)).thenReturn(false);
                Mockito.when(notesService.save(Mockito.any(Note.class))).thenReturn(returnNote);

                mockMvc.perform(
                                put("/notes/" + id.toString())
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .accept(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(dto)))
                                .andDo(print())
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(id.toString()))
                                .andExpect(jsonPath("$.userId").value(user.getId().toString()))
                                .andExpect(jsonPath("$.title").value(dto.getTitle()))
                                .andExpect(jsonPath("$.content").value(dto.getContent()));
        }

        @SuppressWarnings("null")
        @Test
        @WithMockUser
        public void testNoteExists_PutNote_thenOk() throws Exception {
                // set up test data
                UUID id = UUID.randomUUID();
                User user = TestUtils.generateUser();
                NoteDTO dto = new NoteDTO(id, "New Note", "Content.");
                Note returnNote = Note.from(dto);
                returnNote.setId(id);
                returnNote.setUser(user);

                // mock return values
                Mockito.when(userService.getUserByUsername(Mockito.anyString())).thenReturn(user);
                Mockito.when(notesService.exists(id)).thenReturn(true);
                Mockito.when(notesService.save(Mockito.any(Note.class))).thenReturn(returnNote);

                mockMvc.perform(
                                put("/notes/" + id.toString())
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .accept(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(dto)))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(id.toString()))
                                .andExpect(jsonPath("$.userId").value(user.getId().toString()))
                                .andExpect(jsonPath("$.title").value(dto.getTitle()))
                                .andExpect(jsonPath("$.content").value(dto.getContent()));
        }

        @Test
        @WithMockUser
        public void testPutNote_userNotFound() throws Exception {
                User user = TestUtils.generateUser();
                UUID id = UUID.randomUUID();
                NoteDTO testNote = new NoteDTO(id, "Test Note", "Note Content.");

                Mockito.when(userService.getUserByUsername(Mockito.anyString())).thenReturn(null);

                mockMvc.perform(
                                put("/notes/" + id.toString())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .accept(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(testNote)))
                        .andDo(print())
                        .andExpect(status().isNotFound());
        }

        @SuppressWarnings("null")
        @Test
        @WithAnonymousUser
        public void testNoteExists_DeleteNote_thenOk() throws Exception {
                // set up test data
                UUID id = UUID.randomUUID();

                // mock return values
                Mockito.when(notesService.exists(id)).thenReturn(true);

                // test
                mockMvc.perform(delete("/notes/" + id.toString()))
                                .andDo(print())
                                .andExpect(status().isOk());
        }

        @SuppressWarnings("null")
        @Test
        @WithAnonymousUser
        public void testNoteDoesntExist_DeleteNote_thenError() throws Exception {
                // set up test data
                UUID id = UUID.randomUUID();

                // mock results
                Mockito.when(notesService.exists(id)).thenReturn(false);

                // test
                mockMvc.perform(delete("/notes/" + id.toString()))
                                .andDo(print())
                                .andExpect(status().isNotFound());
        }
}
