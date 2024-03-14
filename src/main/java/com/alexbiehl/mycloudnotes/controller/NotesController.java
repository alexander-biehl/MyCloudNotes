package com.alexbiehl.mycloudnotes.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alexbiehl.mycloudnotes.dto.Note;
import com.alexbiehl.mycloudnotes.repository.NotesRepository;

@RestController
public class NotesController {

    @Autowired
    NotesRepository repository;

    // @GetMapping("/notes")
    // public List<Note> GetNotes() {
    // List<Note> notes = repository.findAll();
    // }

}
