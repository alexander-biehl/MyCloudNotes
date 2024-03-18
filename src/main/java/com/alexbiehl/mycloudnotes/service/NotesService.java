package com.alexbiehl.mycloudnotes.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alexbiehl.mycloudnotes.dto.Note;
import com.alexbiehl.mycloudnotes.repository.NotesRepository;

@Service
public class NotesService {

    @Autowired
    private NotesRepository notesRepository;

    public List<Note> getNotes() {
        return notesRepository.findAll();
    }
}
