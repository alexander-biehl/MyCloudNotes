package com.alexbiehl.mycloudnotes.comms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class UserExistsException extends RuntimeException {

    public static final long serialVersionUID = 1L;

    private static final String MESSAGE = "User %s already exists";

    public UserExistsException(UUID id) {
        super(String.format(MESSAGE, id.toString()));
    }

    public UserExistsException(String username) {
        super(String.format(MESSAGE, username));
    }
}
