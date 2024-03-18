package com.alexbiehl.mycloudnotes.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alexbiehl.mycloudnotes.dto.Note;
import com.alexbiehl.mycloudnotes.service.NotesService;

@RestController
@RequestMapping("/notes")
public class NotesController {

    @Autowired
    NotesService notesService;

    @GetMapping("/")
    public List<Note> GetNotes() {
        return notesService.getNotes();
    }

}
