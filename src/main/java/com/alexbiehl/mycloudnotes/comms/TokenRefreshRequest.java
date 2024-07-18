package com.alexbiehl.mycloudnotes.comms;

import org.springframework.lang.NonNull;

public class TokenRefreshRequest {

    @NonNull
    private String refreshToken;

    public TokenRefreshRequest(final String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @NonNull
    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(@NonNull String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
