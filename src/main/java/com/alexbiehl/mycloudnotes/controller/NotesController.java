package com.alexbiehl.mycloudnotes.controller;

import com.alexbiehl.mycloudnotes.api.API;
import com.alexbiehl.mycloudnotes.dto.NoteDTO;
import com.alexbiehl.mycloudnotes.model.Note;
import com.alexbiehl.mycloudnotes.model.User;
import com.alexbiehl.mycloudnotes.service.NotesService;
import com.alexbiehl.mycloudnotes.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping(API.NOTES)
public class NotesController {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotesController.class);

    @Autowired
    private NotesService notesService;

    @Autowired
    private UserService userService;

    @SuppressWarnings("null")
    @PostMapping("")
    public NoteDTO postNote(@NonNull @RequestBody NoteDTO noteDTO,
                            final HttpServletResponse response) {
        LOGGER.info(
                String.format("POST NoteDTO: id: %s, title: %s, content: %s",
                        noteDTO.getId(),
                        noteDTO.getTitle(),
                        noteDTO.getContent()));

        // get authenticated user and ensure they exist
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByUsername(authentication.getName());
        if (user == null) {
            LOGGER.warn(String.format("Unable to find User with username: %s", authentication.getName()));
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("User %s not found", authentication.getName()));
        }

        // convert and save our model
        Note noteModel = Note.from(noteDTO);
        noteModel.setUser(user);
        noteModel = notesService.save(noteModel);

        // set return values
        noteDTO.setId(noteModel.getId());
        noteDTO.setUser(noteModel.getUser().getId());
        response.setStatus(HttpStatus.CREATED.value());
        return noteDTO;
    }

    @GetMapping("")
    @PostFilter("filterObject.userId == authentication.principal.getId() or hasRole('ADMIN')")
    public List<NoteDTO> GetNotes() {
        LOGGER.info("Calling GetNotes");
        return notesService.getNotes()
                .stream()
                .map(NoteDTO::from)
                .collect(Collectors.toList());
    }

    @GetMapping(API.BY_ID)
    public NoteDTO getNoteById(@NonNull @PathVariable("id") UUID id) {
        LOGGER.info(String.format("Calling GetNoteById with id: %s", id));
        Note note = notesService.getNoteById(id);
        if (note == null) {
            LOGGER.warn(String.format("Unable to locate Note Object for ID: %s", id));
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found.");
        }
        return NoteDTO.from(note);
    }

    @SuppressWarnings("null")
    @PutMapping(API.BY_ID)
    @PreAuthorize("hasRole('USER')")
    public NoteDTO updateNote(
            @NonNull @PathVariable("id") UUID id,
            @NonNull @RequestBody NoteDTO updatedNote,
            final HttpServletResponse response) {
        LOGGER.info(String.format("Calling UpdateNote with id: %s", id));

        // get the authenticated user and ensure they are valid
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByUsername(authentication.getName());
        if (user == null) {
            LOGGER.warn(String.format("Unable to find User with username: %s", authentication.getName()));
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("User %s not found", authentication.getName()));
        }

        // prepare our model for saving
        Note savedNote = Note.from(updatedNote);
        savedNote.setUser(user);

        // If the note does not exist, we are creating a new one and thus need to return
        // 201 CREATED
        if (!notesService.exists(id)) {
            LOGGER.debug("PUT note does not exist, creating new");
            response.setStatus(HttpStatus.CREATED.value());
        }
        savedNote = notesService.save(savedNote);

        // set return values
        updatedNote = NoteDTO.from(savedNote);
        updatedNote.setId(savedNote.getId());
        updatedNote.setUser(savedNote.getUser().getId());
        return updatedNote;

    }

    @DeleteMapping(API.BY_ID)
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public void deleteNote(@NonNull @PathVariable("id") UUID id) {
        LOGGER.info(String.format("DeleteNote for id: %s", id));
        LOGGER.info("Notes: {}", notesService.getNotes().toString());

        if (!notesService.exists(id)) {
            LOGGER.warn(String.format("DeleteNote for id %s does not exist", id));
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find a note with the specified ID");
        }
        deleteNote(notesService.getNoteById(id));
    }

    @PreAuthorize("#note.userId == authentication.principal.getId() or hasRole('ADMIN')")
    private void deleteNote(Note note) {
        notesService.deleteById(note.getId());
    }

}
