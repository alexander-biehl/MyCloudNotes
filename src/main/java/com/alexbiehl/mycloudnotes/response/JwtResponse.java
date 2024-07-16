package com.alexbiehl.mycloudnotes.response;

public class JwtResponse {

    private String accessToken;
    // private String refreshToken;
    private String type = "Bearer";

    public JwtResponse(final String accessToken/*, final String refreshToken*/) {
        this.accessToken = accessToken;
        // this.refreshToken = refreshToken);
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JwtResponse that)) return false;

        return getAccessToken().equals(that.getAccessToken()) && getType().equals(that.getType());
    }

    @Override
    public int hashCode() {
        int result = getAccessToken().hashCode();
        result = 31 * result + getType().hashCode();
        return result;
    }
}
