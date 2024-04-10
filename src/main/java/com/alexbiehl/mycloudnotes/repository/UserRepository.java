package com.alexbiehl.mycloudnotes.repository;

import com.alexbiehl.mycloudnotes.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    User findByUsername(final String username);

    boolean existsByUsername(final String username);
}
