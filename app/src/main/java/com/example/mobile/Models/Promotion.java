package com.example.mobile.Models;

import java.util.List;

public class Promotion {
    private String promotionId;
    private String promotionName;
    private int discount;
    private String startDate;
    private String endDate;
    private List<String> productIDs;

    public Promotion(String promotionId, String promotionName, int discount, String startDate, String endDate, List<String> productIDs) {
        this.promotionId = promotionId;
        this.promotionName = promotionName;
        this.discount = discount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.productIDs = productIDs;
    }

    public String getPromotionId() { return promotionId; }
    public String getPromotionName() { return promotionName; }
    public int getDiscount() { return discount; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public List<String> getProductIDs() { return productIDs; }

    public void setPromotionName(String promotionName) { this.promotionName = promotionName; }
    public void setDiscount(int discount) { this.discount = discount; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public void setProductIDs(List<String> productIDs) { this.productIDs = productIDs; }
}