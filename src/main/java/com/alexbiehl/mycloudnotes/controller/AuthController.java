package com.alexbiehl.mycloudnotes.controller;

import com.alexbiehl.mycloudnotes.api.API;
import com.alexbiehl.mycloudnotes.comms.JwtResponse;
import com.alexbiehl.mycloudnotes.comms.LoginRequest;
import com.alexbiehl.mycloudnotes.comms.TokenRefreshRequest;
import com.alexbiehl.mycloudnotes.comms.TokenRefreshResponse;
import com.alexbiehl.mycloudnotes.comms.exception.TokenRefreshException;
import com.alexbiehl.mycloudnotes.components.JwtUtil;
import com.alexbiehl.mycloudnotes.model.RefreshToken;
import com.alexbiehl.mycloudnotes.model.User;
import com.alexbiehl.mycloudnotes.service.RefreshTokenService;
import com.alexbiehl.mycloudnotes.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
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
    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping(API.LOGIN_USER)
    @Transactional
    public ResponseEntity<?> loginJWT(@RequestBody LoginRequest loginRequest) {
        LOGGER.info("Login from: {}", loginRequest.toString());

        User validatedUser = userService.validateUserLogin(loginRequest);

        LOGGER.info("Authenticating user " + loginRequest.getUsername());
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        LOGGER.info("Received authentication: " + authentication.getName() +
                "\nIs authenticated: " + authentication.isAuthenticated());
        LOGGER.info("Setting security context");
        SecurityContextHolder.getContext().setAuthentication(authentication);
        LOGGER.info("Security context set");

        final String token = jwtUtil.createToken(validatedUser);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(validatedUser.getId());
        return ResponseEntity.ok(new JwtResponse(token, refreshToken.getToken().toString()));
    }

    @PostMapping(API.REFRESH_TOKEN)
    @Transactional
    public ResponseEntity<?> refreshToken(@RequestBody TokenRefreshRequest tokenRefreshRequest) {
        String requestRefreshToken = tokenRefreshRequest.getRefreshToken();
        LOGGER.info("Token Refresh Request for token {}", requestRefreshToken);

        try {
            RefreshToken token = refreshTokenService.findByToken(UUID.fromString(requestRefreshToken)).get();
            RefreshToken newToken = refreshTokenService.verifyExpiration(token);
            User user = newToken.getUser();
            String accessToken = jwtUtil.createToken(user);
            LOGGER.info("Successfully refreshed token for user {} at {}", user.getUsername(), new Date());
            return ResponseEntity.ok(new TokenRefreshResponse(newToken.getToken().toString(), accessToken));
        } catch (
                NoSuchElementException nse) {
            LOGGER.error("Unable to locate token for token refresh request {}", requestRefreshToken);
            throw new TokenRefreshException(requestRefreshToken, "Invalid Refresh Token");
        }
    }

//    @PostMapping(API.LOGOUT_USER)
//    @Transactional
//    public ResponseEntity<?> logout() {
//
//    }
}
