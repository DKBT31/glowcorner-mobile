package com.example.mobile.Models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class QuizResponse {
    @SerializedName("status")
    private int status;

    @SerializedName("description")
    private String description;

    @SerializedName("data")
    private List<Quiz> data;

    @SerializedName("success")
    private boolean success;

    public int getStatus() { return status; }
    public String getDescription() { return description; }
    public List<Quiz> getData() { return data; }
    public boolean isSuccess() { return success; }
}
