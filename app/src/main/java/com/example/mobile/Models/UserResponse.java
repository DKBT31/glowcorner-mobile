package com.example.mobile.Models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UserResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("description")
    private String description;

    @SerializedName("data")
    private List<User> data;

    // Getters
    public boolean isSuccess() { return success; }
    public String getDescription() { return description; }
    public List<User> getData() { return data; }
}