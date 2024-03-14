package com.alexbiehl.mycloudnotes.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/health-check")
    public String healthCheck() {
        return "OK";
    }
}
