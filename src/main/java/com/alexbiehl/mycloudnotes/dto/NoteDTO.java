package com.alexbiehl.mycloudnotes.dto;

import java.util.UUID;

import com.alexbiehl.mycloudnotes.model.User;
import org.springframework.lang.NonNull;

import com.alexbiehl.mycloudnotes.model.Note;

public class NoteDTO {

    private UUID id;
    private UUID userId;
    private String title;
    private String content;

    public NoteDTO() {}

    public NoteDTO(UUID id, UUID userId, String title, String content) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.content = content;
    }

    public NoteDTO(UUID userId, String title, String content) {
        this.userId = userId;
        this.title = title;
        this.content = content;
    }

    public NoteDTO(String title, String content) {
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

    public UUID getUserId() {
        return userId;
    }

    public void setUser(UUID userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Note<id = ");
        builder.append(this.id);
        builder.append(" \n");
        builder.append("title = ");
        builder.append(this.title);
        builder.append(" \n");
        builder.append("content = ");
        builder.append(this.content);
        builder.append(" \n>");
        return builder.toString();
    }

    public static NoteDTO from(@NonNull Note note) {
        if (note.getUser() == null) {
            return new NoteDTO(note.getId(), note.getTitle(), note.getContent());
        }
        return new NoteDTO(note.getId(), note.getUser().getId(), note.getTitle(), note.getContent());
    }
}
