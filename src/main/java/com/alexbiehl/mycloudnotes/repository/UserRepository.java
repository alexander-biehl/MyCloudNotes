package com.alexbiehl.mycloudnotes.repository;

import com.alexbiehl.mycloudnotes.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    User findByUsername(final String username);

    @Query(
            value = "IF NOT EXISTS(SELECT * FROM",
            nativeQuery = true
    )
    boolean existsByUsername(final String username);
}
