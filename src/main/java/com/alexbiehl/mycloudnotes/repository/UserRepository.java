package com.alexbiehl.mycloudnotes.repository;

import com.alexbiehl.mycloudnotes.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    User findByUsername(final String username);
}
