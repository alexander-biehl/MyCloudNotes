package com.alexbiehl.mycloudnotes.controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.alexbiehl.mycloudnotes.api.API;
import com.alexbiehl.mycloudnotes.dto.NoteDTO;
import com.alexbiehl.mycloudnotes.model.Note;
import com.alexbiehl.mycloudnotes.service.NotesService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(API.NOTES)
public class NotesController {

    private static Logger LOGGER = LoggerFactory.getLogger(NotesController.class);

    @Autowired
    NotesService notesService;

    @SuppressWarnings("null")
    @PostMapping("")
    public NoteDTO postNote(@NonNull @RequestBody NoteDTO noteDTO, final HttpServletResponse response) {
        LOGGER.info(
                String.format("POST NoteDTO: id: %s, title: %s, content: %s",
                        noteDTO.getId(),
                        noteDTO.getTitle(),
                        noteDTO.getContent()));

        Note noteModel = Note.from(noteDTO);
        noteModel = notesService.save(noteModel);
        noteDTO.setId(noteModel.getId());
        response.setStatus(HttpStatus.CREATED.value());
        return noteDTO;
    }

    @GetMapping("")
    @PostFilter("filterObject.user.id == authentication.principal.getId() or hasRole('ADMIN')")
    public List<NoteDTO> GetNotes() {
        LOGGER.info("Calling GetNotes");
        return notesService.getNotes()
                .stream()
                .map(NoteDTO::from)
                .collect(Collectors.toList());
    }

    @GetMapping(API.BY_ID)
    public NoteDTO getNoteById(@NonNull @PathVariable("id") UUID id) {
        LOGGER.info(String.format("Calling GetNoteById with id: %s", id.toString()));
        Note note = notesService.getNoteById(id);
        if (note == null) {
            LOGGER.warn(String.format("Unable to locate Note Object for ID: %s", id.toString()));
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found.");
        }
        return NoteDTO.from(note);
    }

    @SuppressWarnings("null")
    @PutMapping(API.BY_ID)
    public NoteDTO updateNote(
            @NonNull @PathVariable("id") UUID id,
            @NonNull @RequestBody NoteDTO updatedNote,
            final HttpServletResponse response) {
        LOGGER.info(String.format("Calling UpdateNote with id: %s", id.toString()));

        Note savedNote = Note.from(updatedNote);
        // If the note does not exist, we are creating a new one and thus need to return
        // 201 CREATED
        if (!notesService.exists(id)) {
            LOGGER.debug("PUT note does not exist, creating new");
            savedNote = notesService.save(savedNote);
            response.setStatus(HttpStatus.CREATED.value());
            updatedNote.setId(savedNote.getId());
            return updatedNote;
        } else {
            LOGGER.debug("Updating existing Note.");
            savedNote = notesService.save(savedNote);
            return updatedNote;
        }
    }

    @DeleteMapping(API.BY_ID)
    public void deleteNote(@NonNull @PathVariable("id") UUID id) {
        LOGGER.info(String.format("DeleteNote for id: %s", id.toString()));
        if (!notesService.exists(id)) {
            LOGGER.warn(String.format("DeleteNote for id %s does not exist", id.toString()));
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find a note with the specified ID");
        }
        notesService.deleteById(id);
    }

}
