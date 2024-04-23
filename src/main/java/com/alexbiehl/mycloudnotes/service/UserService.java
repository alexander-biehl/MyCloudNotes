package com.alexbiehl.mycloudnotes.service;

import com.alexbiehl.mycloudnotes.dto.UserLoginDTO;
import com.alexbiehl.mycloudnotes.model.User;
import com.alexbiehl.mycloudnotes.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public User getUserById(@NonNull UUID id) {
        return userRepository.getReferenceById(id);
    }

    public User getUserByUsername(@NonNull String username) {
        return userRepository.findByUsername(username);
    }

    public User validateUserLogin(@NonNull UserLoginDTO userLoginDTO) {
        User user = getUserByUsername(userLoginDTO.getUsername());
        if (user == null) {
            throw new UsernameNotFoundException(String.format("User %s not found", userLoginDTO.getUsername()));
        } else if (user.getUsername().equals(userLoginDTO.getUsername()) &&
                !passwordEncoder.matches(userLoginDTO.getPassword(), user.getPassword())) {
            LOGGER.warn("Password did not match for User: {}", userLoginDTO.getUsername());
            throw new UsernameNotFoundException("Username or password did not match");
        }
        return user;
    }

    public boolean userExistsByUsername(@NonNull String username) {
        return userRepository.findByUsername(username) != null;
    }

    public User saveOrCreate(@NonNull User user) {
        return userRepository.save(user);
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
}
