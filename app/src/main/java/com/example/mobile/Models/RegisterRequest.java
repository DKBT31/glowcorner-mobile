package com.example.mobile.Models;

public class RegisterRequest {
    private String username; // This will be the email for consistency
    private String email;
    private String password;
    private String fullName;
    private String phone;

    public RegisterRequest(String fullName, String email, String phone, String password) {
        this.username = email; // Use email as username for consistent login
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.phone = phone;
    }
}
