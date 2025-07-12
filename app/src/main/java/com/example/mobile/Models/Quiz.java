package com.example.mobile.Models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Quiz {
    @SerializedName("questionId")
    private String questionId;

    @SerializedName("quizText")
    private String quizText;

    @SerializedName("answerOptionDTOS")
    private List<AnswerOption> answerOptionDTOS;

    public String getQuestionId() { return questionId; }
    public String getQuizText() { return quizText; }
    public List<AnswerOption> getAnswerOptionDTOS() { return answerOptionDTOS; }
}
