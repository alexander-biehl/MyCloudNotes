package com.alexbiehl.mycloudnotes.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alexbiehl.mycloudnotes.dto.Note;
import com.alexbiehl.mycloudnotes.service.NotesService;

@RestController
@RequestMapping("/notes")
public class NotesController {

    @Autowired
    NotesService notesService;

    @GetMapping("")
    public List<Note> GetNotes() {
        return notesService.getNotes();
    }

    @GetMapping("/{id}")
    public Note getNoteById(@NonNull @PathVariable("id") UUID id) {
        return notesService.getNoteById(id);
    }

}
