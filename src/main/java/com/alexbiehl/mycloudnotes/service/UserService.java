package com.alexbiehl.mycloudnotes.service;

import com.alexbiehl.mycloudnotes.model.User;
import com.alexbiehl.mycloudnotes.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User getUserById(@NonNull UUID id) {
        return userRepository.getReferenceById(id);
    }

    public User getUserByUsername(@NonNull String username) {
        return userRepository.findByUsername(username);
    }

    public User saveOrCreate(@NonNull User user) {
        return userRepository.save(user);
    }

    public void delete(@NonNull User user) {
        userRepository.delete(user);
    }

    public void delete(@NonNull UUID id) {
        if (!userRepository.existsById(id)) {
            throw new UsernameNotFoundException(String.format("User ID %s not found.", id.toString()));
        } else {
            userRepository.deleteById(id);
        }
    }
}
