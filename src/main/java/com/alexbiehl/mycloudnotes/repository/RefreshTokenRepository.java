package com.alexbiehl.mycloudnotes.repository;

import com.alexbiehl.mycloudnotes.model.RefreshToken;
import com.alexbiehl.mycloudnotes.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByToken(UUID token);

    void deleteByUser(final User user);
}
