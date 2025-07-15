package com.example.mobile.Models;

public class Feedback {
    private String feedbackID;
    private String customerID;
    private int rating;
    private String comment;
    private String feedbackDate;

    // Getters and setters
    public String getFeedbackID() { return feedbackID; }
    public void setFeedbackID(String feedbackID) { this.feedbackID = feedbackID; }
    public String getCustomerID() { return customerID; }
    public void setCustomerID(String customerID) { this.customerID = customerID; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public String getFeedbackDate() { return feedbackDate; }
    public void setFeedbackDate(String feedbackDate) { this.feedbackDate = feedbackDate; }
}