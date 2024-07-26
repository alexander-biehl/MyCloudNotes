package com.alexbiehl.mycloudnotes.service;

import com.alexbiehl.mycloudnotes.model.RefreshToken;
import com.alexbiehl.mycloudnotes.model.User;
import com.alexbiehl.mycloudnotes.repository.RefreshTokenRepository;
import com.alexbiehl.mycloudnotes.repository.UserRepository;
import com.alexbiehl.mycloudnotes.comms.exception.TokenRefreshException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${jwt.refresh.token.validity}")
    private Long refreshTokenValidityMS;

    public Optional<RefreshToken> findByToken(UUID token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken createRefreshToken(UUID userId) {
        RefreshToken token = new RefreshToken();

        token.setUser(userRepository.findById(userId).get());
        token.setExpiryDate(Instant.now().plusMillis(refreshTokenValidityMS));
        token.setToken(UUID.randomUUID());

        token = refreshTokenRepository.save(token);
        return token;
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(
                    token.getToken().toString(),
                    "Refresh token has expired. Please sign in."
            );
        }
        User user = token.getUser();
        // delete existing token to prevent reuse and create a new one
        refreshTokenRepository.deleteByUser(user);

        return createRefreshToken(user.getId());
    }

    @Transactional
    public void deleteByUserId(UUID userId) {
        refreshTokenRepository.deleteByUser(userRepository.findById(userId).get());
    }
}
