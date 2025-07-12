package com.example.mobile.Models;

import java.util.List;

public class Order {
    private String orderID;
    private String customerID;
    private String customerName;
    private String orderDate;
    private String status;
    private double totalAmount;
    private double discountedTotalAmount;
    private List<OrderDetail> orderDetails;
    private String paymentIntentId;
    private String paymentMethodType;
    private String paymentBrand;
    private String paymentLast4;
    private String stripePaymentMethodId;

    public Order(String orderID, String customerID, String customerName, String orderDate, String status,
                 double totalAmount, double discountedTotalAmount, List<OrderDetail> orderDetails,
                 String paymentIntentId, String paymentMethodType, String paymentBrand, String paymentLast4,
                 String stripePaymentMethodId) {
        this.orderID = orderID;
        this.customerID = customerID;
        this.customerName = customerName;
        this.orderDate = orderDate;
        this.status = status;
        this.totalAmount = totalAmount;
        this.discountedTotalAmount = discountedTotalAmount;
        this.orderDetails = orderDetails;
        this.paymentIntentId = paymentIntentId;
        this.paymentMethodType = paymentMethodType;
        this.paymentBrand = paymentBrand;
        this.paymentLast4 = paymentLast4;
        this.stripePaymentMethodId = stripePaymentMethodId;
    }

    public String getOrderID() {
        return orderID;
    }

    public String getCustomerID() {
        return customerID;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public String getStatus() {
        return status;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public double getDiscountedTotalAmount() {
        return discountedTotalAmount;
    }

    public List<OrderDetail> getOrderDetails() {
        return orderDetails;
    }

    public String getPaymentIntentId() {
        return paymentIntentId;
    }

    public String getPaymentMethodType() {
        return paymentMethodType;
    }

    public String getPaymentBrand() {
        return paymentBrand;
    }

    public String getPaymentLast4() {
        return paymentLast4;
    }

    public String getStripePaymentMethodId() {
        return stripePaymentMethodId;
    }
}