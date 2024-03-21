package com.alexbiehl.mycloudnotes.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.alexbiehl.mycloudnotes.model.Note;
import com.alexbiehl.mycloudnotes.repository.NotesRepository;

@Service
public class NotesService {

    @Autowired
    private NotesRepository notesRepository;

    public List<Note> getNotes() {
        return notesRepository.findAll();
    }

    public Note getNoteById(@NonNull UUID id) {
        return notesRepository.getReferenceById(id);
    }

    public boolean exists(@NonNull UUID id) {
        return notesRepository.existsById(id);
    }

    public Note save(@NonNull Note newNote) {
        return notesRepository.save(newNote);
    }

    public void deleteById(@NonNull UUID id) {
        notesRepository.deleteById(id);
    }
}
