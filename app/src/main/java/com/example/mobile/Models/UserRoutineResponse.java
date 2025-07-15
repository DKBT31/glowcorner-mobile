package com.example.mobile.Models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UserRoutineResponse {

    @SerializedName("status")
    private int status;

    @SerializedName("description")
    private String description;

    @SerializedName("data")
    private UserData data;

    @SerializedName("role")
    private String role;

    @SerializedName("redirectUrl")
    private String redirectUrl;

    @SerializedName("success")
    private boolean success;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UserData getData() {
        return data;
    }

    public void setData(UserData data) {
        this.data = data;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public static class UserData {
        @SerializedName("fullName")
        private String fullName;

        @SerializedName("email")
        private String email;

        @SerializedName("phone")
        private String phone;

        @SerializedName("address")
        private String address;

        @SerializedName("skinType")
        private String skinType;

        @SerializedName("avatar_url")
        private String avatarUrl;

        @SerializedName("skinCareRoutine")
        private RoutineData skinCareRoutine;

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getSkinType() {
            return skinType;
        }

        public void setSkinType(String skinType) {
            this.skinType = skinType;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }

        public RoutineData getSkinCareRoutine() {
            return skinCareRoutine;
        }

        public void setSkinCareRoutine(RoutineData skinCareRoutine) {
            this.skinCareRoutine = skinCareRoutine;
        }
    }

    public static class RoutineData {
        @SerializedName("id")
        private String id;

        @SerializedName("routineID")
        private String routineID;

        @SerializedName("skinType")
        private String skinType;

        @SerializedName("routineName")
        private String routineName;

        @SerializedName("routineDescription")
        private String routineDescription;

        @SerializedName("status")
        private String status;

        @SerializedName("products")
        private List<Product> products;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getRoutineID() {
            return routineID;
        }

        public void setRoutineID(String routineID) {
            this.routineID = routineID;
        }

        public String getSkinType() {
            return skinType;
        }

        public void setSkinType(String skinType) {
            this.skinType = skinType;
        }

        public String getRoutineName() {
            return routineName;
        }

        public void setRoutineName(String routineName) {
            this.routineName = routineName;
        }

        public String getRoutineDescription() {
            return routineDescription;
        }

        public void setRoutineDescription(String routineDescription) {
            this.routineDescription = routineDescription;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public List<Product> getProducts() {
            return products;
        }

        public void setProducts(List<Product> products) {
            this.products = products;
        }
    }
}