package com.example.mobile.Models;

public class AddToCartRequest {
    private Integer quantity;

    public AddToCartRequest(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}