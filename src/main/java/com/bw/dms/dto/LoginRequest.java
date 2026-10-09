package com.bw.dms.dto;

import lombok.Data;

@Data 
public class LoginRequest {
    private String username;
    private String password;
    private String email;

    // Getter und Setter
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
