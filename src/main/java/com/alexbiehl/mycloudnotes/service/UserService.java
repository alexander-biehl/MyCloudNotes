package com.alexbiehl.mycloudnotes.service;

import com.alexbiehl.mycloudnotes.comms.LoginRequest;
import com.alexbiehl.mycloudnotes.comms.UserRegisterRequest;
import com.alexbiehl.mycloudnotes.comms.exception.UserExistsException;
import com.alexbiehl.mycloudnotes.model.User;
import com.alexbiehl.mycloudnotes.repository.RoleRepository;
import com.alexbiehl.mycloudnotes.repository.UserRepository;
import io.jsonwebtoken.lang.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public User getUserById(@NonNull UUID id) {
        return userRepository.getReferenceById(id);
    }

    public User getUserByUsername(@NonNull String username) {
        return userRepository.findByUsername(username);
    }

    public User validateUserLogin(@NonNull LoginRequest loginRequest) {
        User user = getUserByUsername(loginRequest.getUsername());
        if (user == null) {
            throw new UsernameNotFoundException(String.format("User %s not found", loginRequest.getUsername()));
        } else if (user.getUsername().equals(loginRequest.getUsername()) &&
                !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            LOGGER.warn("Password did not match for User: {}", loginRequest.getUsername());
            throw new UsernameNotFoundException("Username or password did not match");
        }
        return user;
    }

    public boolean userExistsByUsername(@NonNull String username) {
        return userRepository.findByUsername(username) != null;
    }

    @Transactional
    public User registerUser(@NonNull UserRegisterRequest userRegisterRequest) {
        User savedUser = userRepository.findByUsername(userRegisterRequest.getUsername());
        if (savedUser != null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User already exists");
        }
        savedUser = User.from(userRegisterRequest);
        savedUser.setPassword(passwordEncoder.encode(savedUser.getPassword()));
        savedUser.setRoles(Collections.setOf(roleRepository.findByName("USER")));
        return userRepository.save(savedUser);
    }

    public void delete(@NonNull User user) {
        userRepository.delete(user);
    }

    public void delete(@NonNull UUID id) {
        if (!userRepository.existsById(id)) {
            throw new UsernameNotFoundException(String.format("User ID %s not found.", id));
        } else {
            userRepository.deleteById(id);
        }
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User updateUser(User user) {
        if (!userRepository.existsById(user.getId())) {
            throw new UsernameNotFoundException(String.format("User ID %s not found.", user.getId().toString()));
        } else {
            return userRepository.save(user);
        }
    }

    public User save(User user) {
        if (userRepository.existsById(user.getId())) {
            throw new UserExistsException(user.getUsername());
        } else {
            return userRepository.save(user);
        }
    }
}
