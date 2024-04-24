package com.alexbiehl.mycloudnotes.controller;

import com.alexbiehl.mycloudnotes.api.API;
import com.alexbiehl.mycloudnotes.components.JwtUtil;
import com.alexbiehl.mycloudnotes.dto.UserDTO;
import com.alexbiehl.mycloudnotes.dto.UserLoginDTO;
import com.alexbiehl.mycloudnotes.model.User;
import com.alexbiehl.mycloudnotes.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(API.USERS)
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping(API.REGISTER_USER)
    public void register(@NonNull @RequestBody UserDTO userDTO) {
        LOGGER.info("Register request for UserDTO: {}", userDTO);

    }

    @PostMapping(API.LOGIN_USER)
    public ResponseEntity login(@RequestBody UserLoginDTO userLogin) {
        LOGGER.info("Login from: {}", userLogin.toString());
        User validatedUser = userService.validateUserLogin(userLogin);
        final String token = String.format("%s %s", JwtUtil.TOKEN_PREFIX, jwtUtil.createToken(validatedUser));
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, token).build();
    }
}
