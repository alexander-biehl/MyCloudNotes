package com.alexbiehl.mycloudnotes.repository;

import com.alexbiehl.mycloudnotes.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
}
