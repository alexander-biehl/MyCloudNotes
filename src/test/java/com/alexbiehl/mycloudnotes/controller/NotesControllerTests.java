package com.alexbiehl.mycloudnotes.controller;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.alexbiehl.mycloudnotes.MycloudnotesApplication;
import com.alexbiehl.mycloudnotes.dto.NoteDTO;
import com.alexbiehl.mycloudnotes.model.Note;
import com.alexbiehl.mycloudnotes.service.NotesService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { MycloudnotesApplication.class })
@AutoConfigureMockMvc
public class NotesControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotesService notesService;

    @Test
    public void withoutData_testGetNotesEndpoint() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.get("/notes")
                        .accept(MediaType.APPLICATION_JSON))
                // .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[*]").isEmpty());
    }

    @Test
    public void withData_testGetNoteEndpoint() throws Exception {
        // Define sample data
        UUID id = UUID.randomUUID();
        Note note = new Note(id, "TestNote", "Test note content.");

        // Mock the Service behaviour
        Mockito.when(notesService.getNoteById(id)).thenReturn(note);

        // perform the test
        mockMvc.perform(
                MockMvcRequestBuilders.get("/notes/" + id.toString())
                        .accept(MediaType.APPLICATION_JSON))
                // .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(note.getId().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(note.getTitle()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content").value(note.getContent()));
    }

    @Test
    public void withoutData_testGetNoteEndpoint_andIsNotFound() throws Exception {
        UUID id = UUID.randomUUID();

        // mock the service behaviour
        Mockito.when(notesService.getNoteById(id)).thenReturn(null);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/notes/" + id.toString())
                        .accept(MediaType.APPLICATION_JSON))
                // .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void testPostNote_andOk() throws Exception {
        // set up test data
        NoteDTO testNote = new NoteDTO(null, "Test Note", "Note Content.");
        Note toReturnNote = Note.from(testNote);
        toReturnNote.setId(UUID.randomUUID());

        // mock return values
        Mockito.when(notesService.save(Mockito.any(Note.class))).thenReturn(toReturnNote);

        // perform test
        mockMvc.perform(
                MockMvcRequestBuilders.post("/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testNote)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());
    }
}
