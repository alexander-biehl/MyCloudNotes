package com.alexbiehl.mycloudnotes.controller.advice;

import com.alexbiehl.mycloudnotes.comms.exception.UserExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;

@RestControllerAdvice
public class UserExistsAdvice {

    @ExceptionHandler(value = UserExistsException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorMessage handleError(
            UserExistsException ex,
            WebRequest request
    ) {
        return new ErrorMessage(
                HttpStatus.FORBIDDEN.value(),
                new Date(),
                ex.getMessage(),
                request.getDescription(false)
        );
    }
}