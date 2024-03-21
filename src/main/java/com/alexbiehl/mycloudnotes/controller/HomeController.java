package com.alexbiehl.mycloudnotes.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alexbiehl.mycloudnotes.api.API;

@RestController
public class HomeController {

    @GetMapping(API.HEALTH_CHECK)
    public String healthCheck() {
        return "OK";
    }
}
