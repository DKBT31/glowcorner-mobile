package com.example.mobile.Models;

public class AddToCartResponse {
    private int status;
    private String description;
    private Object data; // Using Object for empty {} or can be a specific type if known
    private String role;
    private String redirectUrl;
    private boolean success;

    public int getStatus() { return status; }
    public String getDescription() { return description; }
    public Object getData() { return data; }
    public String getRole() { return role; }
    public String getRedirectUrl() { return redirectUrl; }
    public boolean isSuccess() { return success; }

    public void setStatus(int status) { this.status = status; }
    public void setDescription(String description) { this.description = description; }
    public void setData(Object data) { this.data = data; }
    public void setRole(String role) { this.role = role; }
    public void setRedirectUrl(String redirectUrl) { this.redirectUrl = redirectUrl; }
    public void setSuccess(boolean success) { this.success = success; }
}