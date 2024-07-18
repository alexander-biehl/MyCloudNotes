package com.alexbiehl.mycloudnotes.comms;

public class TokenRefreshResponse {

    private String refreshToken;
    private String accessToken;
    private String tokenType = "Bearer";

    public TokenRefreshResponse(final String refreshToken, final String accessToken) {
        this.refreshToken = refreshToken;
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
}
