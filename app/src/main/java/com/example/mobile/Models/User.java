package com.example.mobile.Models;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("userID")
    private String userID;
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
    @SerializedName("loyalPoints")
    private int loyalPoints;
    @SerializedName("role")
    private String role;
    @SerializedName("avatar_url")
    private String avatar_url;
    @SerializedName("orders")
    private Object orders; // Can be null or a list
    @SerializedName("skinCareRoutine")
    private Routine skinCareRoutine;

    // Getters and Setters
    public String getUserID() { return userID; }
    public void setUserID(String userID) { this.userID = userID; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getSkinType() { return skinType; }
    public void setSkinType(String skinType) { this.skinType = skinType; }
    public int getLoyalPoints() { return loyalPoints; }
    public void setLoyalPoints(int loyalPoints) { this.loyalPoints = loyalPoints; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getAvatar_url() { return avatar_url; }
    public void setAvatar_url(String avatar_url) { this.avatar_url = avatar_url; }
    public Object getOrders() { return orders; }
    public void setOrders(Object orders) { this.orders = orders; }
    public Routine getSkinCareRoutine() { return skinCareRoutine; }
    public void setSkinCareRoutine(Routine skinCareRoutine) { this.skinCareRoutine = skinCareRoutine; }
}