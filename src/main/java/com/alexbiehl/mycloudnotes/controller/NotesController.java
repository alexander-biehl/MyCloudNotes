package com.alexbiehl.mycloudnotes.controller;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.alexbiehl.mycloudnotes.dto.Note;
import com.alexbiehl.mycloudnotes.service.NotesService;

@RestController
@RequestMapping("/notes")
public class NotesController {

    private static Logger LOGGER = LoggerFactory.getLogger(NotesController.class);

    @Autowired
    NotesService notesService;

    @GetMapping("")
    public List<Note> GetNotes() {
        LOGGER.info("Calling GetNotes");
        return notesService.getNotes();
    }

    @GetMapping("/{id}")
    public Note getNoteById(@NonNull @PathVariable("id") UUID id) {
        LOGGER.info("Calling GetNoteById with id: %s", id);
        Note note = notesService.getNoteById(id);
        if (note == null) {
            LOGGER.warn("Unable to locate Note Object for ID: %s", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found.");
        }
        return note;
    }

}
