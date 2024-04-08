package com.alexbiehl.mycloudnotes.dto;

import com.alexbiehl.mycloudnotes.model.User;
import org.springframework.lang.NonNull;

import java.util.UUID;

public class UserDTO {

    private boolean active;
    private String username;
    private UUID id;

    public UserDTO(boolean active, String username, UUID id) {
        this.active = active;
        this.username = username;
        this.id = id;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder
                .append("UserDTO<")
                .append("active = ").append(this.active)
                .append(", id = ").append(this.id.toString())
                .append(", username = ").append(this.username)
                .append(">");
        return builder.toString();
    }

    public static UserDTO from(@NonNull User user) {
        return new UserDTO(user.isActive(), user.getUsername(), user.getId());
    }
}
