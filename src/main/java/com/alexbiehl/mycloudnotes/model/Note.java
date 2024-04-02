package com.alexbiehl.mycloudnotes.model;

import java.util.UUID;

import com.alexbiehl.mycloudnotes.dto.NoteDTO;

import jakarta.persistence.*;

@Entity
@Table(name = "notes")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @OneToOne
    private UUID user_id;
    private String content;
    private String title;

    @SuppressWarnings("unused")
    public Note() {
    }

    public Note(UUID id, UUID user_id, String title, String content) {
        this.id = id;
        this.user_id = user_id;
        this.title = title;
        this.content = content;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public UUID getUserId() {
        return user_id;
    }

    public void setUserId(UUID user_id) {
        this.user_id = user_id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Note note)) return false;

        if (!getId().equals(note.getId())) return false;
        if (!user_id.equals(note.user_id)) return false;
        if (getContent() != null ? !getContent().equals(note.getContent()) : note.getContent() != null) return false;
        return getTitle().equals(note.getTitle());
    }

    @Override
    public int hashCode() {
        int result = getId().hashCode();
        result = 31 * result + user_id.hashCode();
        result = 31 * result + (getContent() != null ? getContent().hashCode() : 0);
        result = 31 * result + getTitle().hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Note<id = ");
        builder.append(this.id);
        builder.append(" \n");
        builder.append("userID = ");
        builder.append(this.user_id);
        builder.append(" \n");
        builder.append("title = ");
        builder.append(this.title);
        builder.append(" \n");
        builder.append("content = ");
        builder.append(this.content);
        builder.append(" \n>");
        return builder.toString();
    }

    public static Note from(NoteDTO note) {
        return new Note(note.getId(), note.getUserId(), note.getTitle(), note.getContent());
    }
}
