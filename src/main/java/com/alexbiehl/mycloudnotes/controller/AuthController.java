package com.alexbiehl.mycloudnotes.controller;

import com.alexbiehl.mycloudnotes.api.API;
import com.alexbiehl.mycloudnotes.comms.TokenRefreshRequest;
import com.alexbiehl.mycloudnotes.comms.TokenRefreshResponse;
import com.alexbiehl.mycloudnotes.comms.exception.TokenRefreshException;
import com.alexbiehl.mycloudnotes.components.JwtUtil;
import com.alexbiehl.mycloudnotes.dto.UserLoginDTO;
import com.alexbiehl.mycloudnotes.model.RefreshToken;
import com.alexbiehl.mycloudnotes.model.User;
import com.alexbiehl.mycloudnotes.comms.JwtResponse;
import com.alexbiehl.mycloudnotes.service.RefreshTokenService;
import com.alexbiehl.mycloudnotes.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping(API.AUTH)
public class AuthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private RefreshTokenService refreshTokenService;

    @PostMapping(API.LOGIN_USER)
    public ResponseEntity<?> loginJWT(@RequestBody UserLoginDTO userLogin) {
        LOGGER.info("Login from: {}", userLogin.toString());
        User validatedUser = userService.validateUserLogin(userLogin);
        final String token = jwtUtil.createToken(validatedUser);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(validatedUser.getId());
        return ResponseEntity.ok(new JwtResponse(token, refreshToken.getToken().toString()));
    }

    @PostMapping(API.REFRESH_TOKEN)
    @Transactional
    public ResponseEntity<?> refreshToken(@RequestBody TokenRefreshRequest tokenRefreshRequest) {
        String requestRefreshToken = tokenRefreshRequest.getRefreshToken();

        try {
            RefreshToken token = refreshTokenService.findByToken(UUID.fromString(requestRefreshToken)).get();
            RefreshToken newToken = refreshTokenService.verifyExpiration(token);
            User user = newToken.getUser();
            String accessToken = jwtUtil.createToken(user);
            return ResponseEntity.ok(new TokenRefreshResponse(newToken.getToken().toString(), accessToken));
        } catch (NoSuchElementException nse) {
            throw new TokenRefreshException(requestRefreshToken, "Invalid Refresh Token");
        }
    }
}
