package com.alexbiehl.mycloudnotes.controller.advice;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;

@RestControllerAdvice
public class UsernameNotFoundAdvice {

    @ExceptionHandler(value = UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorMessage handleUsernameNotFoundException(
            UsernameNotFoundException usernameNotFoundException,
            WebRequest request) {
        return new ErrorMessage(
                HttpStatus.FORBIDDEN.value(),
                new Date(),
                usernameNotFoundException.getMessage(),
                request.getDescription(false)
        );
    }
}
