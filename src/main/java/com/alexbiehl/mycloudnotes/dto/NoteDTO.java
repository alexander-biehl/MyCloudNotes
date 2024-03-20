package com.alexbiehl.mycloudnotes.dto;

import java.util.UUID;

import org.springframework.lang.NonNull;

import com.alexbiehl.mycloudnotes.model.Note;

public class NoteDTO {

    private UUID id;
    private String title;
    private String content;

    public NoteDTO(UUID id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public static NoteDTO from(@NonNull Note note) {
        return new NoteDTO(note.getId(), note.getTitle(), note.getContent());
    }
}
