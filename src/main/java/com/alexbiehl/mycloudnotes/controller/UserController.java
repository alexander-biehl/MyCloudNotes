package com.alexbiehl.mycloudnotes.controller;

import com.alexbiehl.mycloudnotes.api.API;
import com.alexbiehl.mycloudnotes.comms.UserRegisterRequest;
import com.alexbiehl.mycloudnotes.dto.UserDTO;
import com.alexbiehl.mycloudnotes.model.User;
import com.alexbiehl.mycloudnotes.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(API.USERS)
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @PostMapping(API.REGISTER_USER)
    public UserDTO register(@NonNull @RequestBody UserRegisterRequest userRegisterRequest, HttpServletResponse response) {
        LOGGER.info("Register request for UserDTO: {}", userRegisterRequest.toString());
        // register our user, throws an exception if the username already exists
        User registeredUser = userService.registerUser(userRegisterRequest);
        response.setStatus(HttpStatus.CREATED.value());
        return UserDTO.from(registeredUser);
    }

    @GetMapping(API.BY_ID)
    @PreAuthorize("hasRole('USER')")
    public UserDTO getUserById(@PathVariable UUID id) {
        LOGGER.info("GetUserById: {}", id.toString());
        User user = userService.getUserById(id);
        return UserDTO.from(user);
    }

    @PutMapping(API.BY_ID)
    @PreAuthorize("hasRole('ADMIN')")
    public UserDTO putUserById(@RequestBody UserDTO userDTO) {
        LOGGER.info("PUT Request for user: {}", userDTO.toString());
        return UserDTO.from(userService.updateUser(User.from(userDTO)));
    }

    @GetMapping(API.BY_USERNAME)
    @PreAuthorize("hasRole('USER')")
    public UserDTO getUserByUsername(@PathVariable String username) {
        LOGGER.info("GetUserByUsername: {}", username);
        User user = userService.getUserByUsername(username);
        return UserDTO.from(user);
    }

    @GetMapping("")
    @PostFilter("filterObject.userId == authentication.principal.getId() or hasRole('ADMIN')")
    public List<UserDTO> getAllUsers() {
        LOGGER.info("getAllUsers called");
        return userService.findAll()
                .stream()
                .map(UserDTO::from)
                .toList();
    }

    @PostMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDTO postUser(@RequestBody UserDTO userDTO, HttpServletResponse response) {
        LOGGER.info("POST request for UserDTO: {}", userDTO.toString());
        User savedUser = userService.save(User.from(userDTO));
        response.setStatus(HttpStatus.CREATED.value());
        return UserDTO.from(savedUser);
    }
}
