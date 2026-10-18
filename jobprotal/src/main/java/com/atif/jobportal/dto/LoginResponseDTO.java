
package com.atif.jobportal.dto;

public class LoginResponseDTO {

    private final String token;
    private final String tokenType = "Bearer";
    private final String role;

    public LoginResponseDTO(String token, String role) {
        this.token = token;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getRole() {
        return role;
    }
}
