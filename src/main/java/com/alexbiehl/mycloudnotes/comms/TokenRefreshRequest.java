package com.alexbiehl.mycloudnotes.comms;

import org.springframework.lang.NonNull;


public class TokenRefreshRequest {

    private String refreshToken;

    public TokenRefreshRequest() {}

    public TokenRefreshRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @NonNull
    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
