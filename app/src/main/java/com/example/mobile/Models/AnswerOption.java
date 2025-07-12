package com.example.mobile.Models;

import com.google.gson.annotations.SerializedName;

public class AnswerOption {
    @SerializedName("questionID")
    private String questionID;

    @SerializedName("optionID")
    private String optionID;

    @SerializedName("skinType")
    private String skinType;

    @SerializedName("optionText")
    private String optionText;

    public String getQuestionID() { return questionID; }
    public String getOptionID() { return optionID; }
    public String getSkinType() { return skinType; }
    public String getOptionText() { return optionText; }
}
