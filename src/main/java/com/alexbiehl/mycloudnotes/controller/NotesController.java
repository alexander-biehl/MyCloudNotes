package com.alexbiehl.mycloudnotes.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alexbiehl.mycloudnotes.repository.NotesRepository;

@RestController
public class NotesController {

    @Autowired
    NotesRepository repository;

    @GetMapping("/health-check")
    public String healthCheck() {
        return "OK";
    }

}
