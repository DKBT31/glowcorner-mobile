package com.example.mobile.Models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RoutineResponse {
    @SerializedName("status")
    private int status;

    @SerializedName("description")
    private String description;

    @SerializedName("data")
    private RoutineData data;

    @SerializedName("role")
    private String role;

    @SerializedName("redirectUrl")
    private String redirectUrl;

    @SerializedName("success")
    private boolean success;

    // Getters
    public int getStatus() { return status; }
    public String getDescription() { return description; }
    public RoutineData getData() { return data; }
    public String getRole() { return role; }
    public String getRedirectUrl() { return redirectUrl; }
    public boolean isSuccess() { return success; }

    public static class RoutineData {
        @SerializedName("routineID")
        private String routineID;

        @SerializedName("skinType")
        private String skinType;

        @SerializedName("routineName")
        private String routineName;

        @SerializedName("routineDescription")
        private String routineDescription;

        @SerializedName("productDTOS")
        private List<Product> productDTOS;

        // Getters
        public String getRoutineID() { return routineID; }
        public String getSkinType() { return skinType; }
        public String getRoutineName() { return routineName; }
        public String getRoutineDescription() { return routineDescription; }
        public List<Product> getProductDTOS() { return productDTOS; }
    }
}