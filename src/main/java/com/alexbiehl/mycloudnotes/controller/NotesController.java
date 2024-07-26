package com.alexbiehl.mycloudnotes.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.alexbiehl.mycloudnotes.model.User;
import com.alexbiehl.mycloudnotes.security.UserPrincipal;
import com.alexbiehl.mycloudnotes.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
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
    @PreAuthorize("hasRole('USER')")
    @PostFilter("filterObject.userId == authentication.principal.getId() or hasRole('ADMIN')")
    public List<NoteDTO> GetNotes(Authentication authentication) {
        LOGGER.info("Calling GetNotes");
        LOGGER.info("auth: " + authentication.getName());
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
    public void deleteNote(@NonNull @PathVariable("id") UUID id) {
        LOGGER.info(String.format("DeleteNote for id: %s", id.toString()));
        if (!notesService.exists(id)) {
            LOGGER.warn(String.format("DeleteNote for id %s does not exist", id.toString()));
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find a note with the specified ID");
        }
        notesService.deleteById(id);
    }

}
