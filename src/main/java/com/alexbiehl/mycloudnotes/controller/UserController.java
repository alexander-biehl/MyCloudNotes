package com.alexbiehl.mycloudnotes.controller;

import com.alexbiehl.mycloudnotes.api.API;
import com.alexbiehl.mycloudnotes.components.JwtUtil;
import com.alexbiehl.mycloudnotes.dto.UserDTO;
import com.alexbiehl.mycloudnotes.dto.UserLoginDTO;
import com.alexbiehl.mycloudnotes.model.User;
import com.alexbiehl.mycloudnotes.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(API.USERS)
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping(API.REGISTER_USER)
    public UserDTO register(@NonNull @RequestBody UserDTO userDTO, HttpServletResponse response) {
        LOGGER.info("Register request for UserDTO: {}", userDTO);
        // register our user, throws an exception if the username already exists
        User registeredUser = userService.registerUser(userDTO);
        response.setStatus(HttpStatus.CREATED.value());
        return UserDTO.from(registeredUser);
    }

    @PostMapping(API.LOGIN_USER)
    public ResponseEntity login(@RequestBody UserLoginDTO userLogin) {
        LOGGER.info("Login from: {}", userLogin.toString());
        User validatedUser = userService.validateUserLogin(userLogin);
        final String token = String.format("%s %s", JwtUtil.TOKEN_PREFIX, jwtUtil.createToken(validatedUser));
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, token).build();
    }

    @GetMapping(API.BY_ID)
    public UserDTO getUserById(@PathVariable UUID id) {
        LOGGER.info("GetUserById: {}", id.toString());
        User user = userService.getUserById(id);
        return UserDTO.from(user);
    }

    @GetMapping(API.BY_USERNAME)
    public UserDTO getUserByUsername(@PathVariable String username) {
        LOGGER.info("GetUserByUsername: {}", username);
        User user = userService.getUserByUsername(username);
        return UserDTO.from(user);
    }
}
