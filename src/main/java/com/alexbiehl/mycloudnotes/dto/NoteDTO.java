package com.alexbiehl.mycloudnotes.dto;

import java.util.UUID;

import com.alexbiehl.mycloudnotes.model.User;
import org.springframework.lang.NonNull;

import com.alexbiehl.mycloudnotes.model.Note;

public class NoteDTO {

    private UUID id;
    private UserDTO user;
    private String title;
    private String content;

    public NoteDTO() {}

    public NoteDTO(UUID id, UserDTO user, String title, String content) {
        this.id = id;
        this.user = user;
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

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
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
        return new NoteDTO(note.getId(), UserDTO.from(note.getUser()), note.getTitle(), note.getContent());
    }
}
