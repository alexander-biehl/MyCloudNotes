package com.alexbiehl.mycloudnotes.controller;

import com.alexbiehl.mycloudnotes.api.API;
import com.alexbiehl.mycloudnotes.components.JwtUtil;
import com.alexbiehl.mycloudnotes.dto.UserLoginDTO;
import com.alexbiehl.mycloudnotes.model.User;
import com.alexbiehl.mycloudnotes.response.JwtResponse;
import com.alexbiehl.mycloudnotes.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(API.AUTH)
public class AuthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping(API.LOGIN_USER)
    public ResponseEntity<?> loginJWT(@RequestBody UserLoginDTO userLogin) {
        LOGGER.info("Login from: {}", userLogin.toString());
        User validatedUser = userService.validateUserLogin(userLogin);
        final String token = String.format("%s %s", JwtUtil.TOKEN_PREFIX, jwtUtil.createToken(validatedUser));
        return ResponseEntity.ok(new JwtResponse(token));
    }
}
