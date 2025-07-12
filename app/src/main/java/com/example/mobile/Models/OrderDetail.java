package com.example.mobile.Models;

public class OrderDetail {
    private String orderID;
    private String productID;
    private String productName;
    private int quantity;
    private double productPrice;
    private String discountName;
    private Double discountPercentage; // Nullable
    private double totalAmount;
    private Double discountedTotalAmount; // Nullable

    public OrderDetail(String orderID, String productID, String productName, int quantity, double productPrice,
                       String discountName, Double discountPercentage, double totalAmount, Double discountedTotalAmount) {
        this.orderID = orderID;
        this.productID = productID;
        this.productName = productName;
        this.quantity = quantity;
        this.productPrice = productPrice;
        this.discountName = discountName;
        this.discountPercentage = discountPercentage;
        this.totalAmount = totalAmount;
        this.discountedTotalAmount = discountedTotalAmount;
    }

    public String getOrderID() {
        return orderID;
    }

    public String getProductID() {
        return productID;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public String getDiscountName() {
        return discountName;
    }

    public Double getDiscountPercentage() {
        return discountPercentage;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public Double getDiscountedTotalAmount() {
        return discountedTotalAmount;
    }
}