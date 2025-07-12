package com.example.mobile.Models;

import com.google.gson.annotations.SerializedName;

public class OrderResponse {
    @SerializedName("status")
    private int status;

    @SerializedName("description")
    private String description;

    @SerializedName("data")
    private OrderData data;

    @SerializedName("success")
    private boolean success;

    // Getters
    public int getStatus() { return status; }
    public String getDescription() { return description; }
    public OrderData getData() { return data; }
    public boolean isSuccess() { return success; }

    public static class OrderData {
        @SerializedName("orderID")
        private String orderID;

        @SerializedName("customerID")
        private String customerID;

        @SerializedName("customerName")
        private String customerName;

        @SerializedName("orderDate")
        private String orderDate;

        @SerializedName("status")
        private String status;

        @SerializedName("totalAmount")
        private double totalAmount;

        @SerializedName("discountedTotalAmount")
        private double discountedTotalAmount;

        @SerializedName("orderDetails")
        private OrderDetail[] orderDetails;

        @SerializedName("paymentIntentId")
        private String paymentIntentId;

        @SerializedName("paymentMethodType")
        private String paymentMethodType;

        @SerializedName("paymentBrand")
        private String paymentBrand;

        @SerializedName("paymentLast4")
        private String paymentLast4;

        @SerializedName("stripePaymentMethodId")
        private String stripePaymentMethodId;

        // Getters
        public String getOrderID() { return orderID; }
        public String getCustomerID() { return customerID; }
        public String getCustomerName() { return customerName; }
        public String getOrderDate() { return orderDate; }
        public String getStatus() { return status; }
        public double getTotalAmount() { return totalAmount; }
        public double getDiscountedTotalAmount() { return discountedTotalAmount; }
        public OrderDetail[] getOrderDetails() { return orderDetails; }
        public String getPaymentIntentId() { return paymentIntentId; }
        public String getPaymentMethodType() { return paymentMethodType; }
        public String getPaymentBrand() { return paymentBrand; }
        public String getPaymentLast4() { return paymentLast4; }
        public String getStripePaymentMethodId() { return stripePaymentMethodId; }
    }

    public static class OrderDetail {
        @SerializedName("productID")
        private String productID;

        @SerializedName("quantity")
        private int quantity;

        @SerializedName("productPrice")
        private double productPrice;

        @SerializedName("totalAmount")
        private double totalAmount;

        // Getters
        public String getProductID() { return productID; }
        public int getQuantity() { return quantity; }
        public double getProductPrice() { return productPrice; }
        public double getTotalAmount() { return totalAmount; }
    }
}