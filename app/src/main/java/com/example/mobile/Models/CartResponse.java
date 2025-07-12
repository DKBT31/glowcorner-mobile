package com.example.mobile.Models;

import java.util.List;

public class CartResponse {
    private String userID;
    private List<Item> items;
    private int totalAmount;
    private Integer discountedTotalAmount; // Nullable

    public static class Item {
        private String userID;
        private String productID;
        private String productName;
        private int productPrice;
        private int quantity;
        private int totalAmount;
        private Integer discountPercentage; // Nullable
        private Integer discountedTotalAmount; // Nullable


        public String getUserID() { return userID; }
        public String getProductID() { return productID; }
        public String getProductName() { return productName; }
        public int getProductPrice() { return productPrice; }
        public int getQuantity() { return quantity; }
        public int getTotalAmount() { return totalAmount; }
        public Integer getDiscountPercentage() { return discountPercentage; }
        public Integer getDiscountedTotalAmount() { return discountedTotalAmount; }
    }

    public String getUserID() { return userID; }
    public List<Item> getItems() { return items; }
    public int getTotalAmount() { return totalAmount; }
    public Integer getDiscountedTotalAmount() { return discountedTotalAmount; }
}