package com.example.mobile.staff;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.mobile.Api.ApiClient;
import com.example.mobile.Api.ApiService;
import com.example.mobile.R;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StaffSkinQuizActivity extends AppCompatActivity {
    private static final String TAG = "StaffSkinQuizActivity";
    private LinearLayout quizContainer;
    private LinearLayout dynamicContent;
    private Button createButton;

    private List<Quiz> quizzes = new ArrayList<>();
    private Map<String, Integer> skinTypeCounts = new HashMap<>();
    private Map<String, String> selectedAnswers = new HashMap<>(); // Map questionId to selected optionText

    private static class Quiz {
        String questionId;
        String quizText;
        List<AnswerOption> answerOptionDTOS;

        Quiz(String questionId, String quizText, List<AnswerOption> answerOptionDTOS) {
            this.questionId = questionId;
            this.quizText = quizText;
            this.answerOptionDTOS = answerOptionDTOS;
        }
    }

    private static class AnswerOption {
        String optionID;
        String optionText;
        String questionID;
        String skinType;

        AnswerOption(String optionID, String optionText, String questionID, String skinType) {
            this.optionID = optionID;
            this.optionText = optionText;
            this.questionID = questionID;
            this.skinType = skinType;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.staff_skin_quiz);

        quizContainer = findViewById(R.id.quiz_container);
        dynamicContent = findViewById(R.id.dynamic_content);
        createButton = findViewById(R.id.createButton);

        createButton.setOnClickListener(v -> startActivity(new Intent(this, CreateQuizActivity.class)));

        Log.d(TAG, "onCreate: Initialized UI components");
        loadQuizzes();
    }

    private void loadQuizzes() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ResponseBody> call = apiService.getQuizzes();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseString = response.body().string();
                        Log.d(TAG, "Quiz Response: " + responseString);

                        JSONObject jsonObject = new JSONObject(responseString);
                        if (jsonObject.getBoolean("success") && jsonObject.getInt("status") == 200) {
                            JsonParser parser = new JsonParser();
                            JsonArray dataArray = parser.parse(responseString).getAsJsonObject().getAsJsonArray("data");
                            Log.d(TAG, "Number of quizzes in response: " + dataArray.size());
                            quizzes.clear();
                            for (JsonElement element : dataArray) {
                                JsonObject quizObj = element.getAsJsonObject();
                                String questionId = quizObj.get("questionId").getAsString();
                                String quizText = quizObj.get("quizText").getAsString();
                                JsonArray optionsArray = quizObj.getAsJsonArray("answerOptionDTOS");
                                List<AnswerOption> options = new ArrayList<>();
                                for (JsonElement optionElement : optionsArray) {
                                    JsonObject optionObj = optionElement.getAsJsonObject();
                                    options.add(new AnswerOption(
                                            optionObj.get("optionID").getAsString(),
                                            optionObj.get("optionText").getAsString(),
                                            optionObj.get("questionID").getAsString(),
                                            optionObj.get("skinType").getAsString()
                                    ));
                                }
                                quizzes.add(new Quiz(questionId, quizText, options));
                            }
                            Log.d(TAG, "Loaded " + quizzes.size() + " quizzes");
                            displayAllQuestions();
                        } else {
                            Log.e(TAG, "API Error: " + jsonObject.getString("description"));
                            Toast.makeText(StaffSkinQuizActivity.this, "Failed to load quizzes: " + jsonObject.getString("description"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "Error parsing quiz response: " + e.getMessage(), e);
                        Toast.makeText(StaffSkinQuizActivity.this, "Error reading quiz data", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Response not successful. Status: " + response.code());
                    Toast.makeText(StaffSkinQuizActivity.this, "Failed to load quizzes. Status: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "API Call Failed for quizzes: " + t.getMessage(), t);
                Toast.makeText(StaffSkinQuizActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayAllQuestions() {
        dynamicContent.removeAllViews();
        if (quizzes.isEmpty()) {
            Log.w(TAG, "No quizzes to display");
            Toast.makeText(this, "No quizzes available", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Quiz quiz : quizzes) {
            CardView cardView = (CardView) getLayoutInflater().inflate(R.layout.staff_item_quiz, dynamicContent, false);
            TextView quizTextView = cardView.findViewById(R.id.quizText);
            LinearLayout optionsContainer = cardView.findViewById(R.id.optionsContainer);

            quizTextView.setText(quiz.quizText);
            Log.d(TAG, "Displaying question: " + quiz.quizText + " with " + quiz.answerOptionDTOS.size() + " options");

            for (int i = 0; i < quiz.answerOptionDTOS.size(); i++) {
                AnswerOption option = quiz.answerOptionDTOS.get(i);
                TextView optionTextView = new TextView(this);
                optionTextView.setId(View.generateViewId());
                optionTextView.setText(String.format("%d. %s", i + 1, option.optionText));
                optionTextView.setTextSize(16);
                optionTextView.setTextColor(0xFF666666);
                optionTextView.setPadding(8, 8, 8, 8);
                optionTextView.setBackgroundResource(android.R.drawable.list_selector_background);
                optionsContainer.addView(optionTextView);
            }

            cardView.setOnClickListener(v -> {
                Intent intent = new Intent(StaffSkinQuizActivity.this, EditQuizActivity.class);
                intent.putExtra("questionId", quiz.questionId);
                startActivity(intent);
            });

            dynamicContent.addView(cardView);
        }
    }

    private String determineSkinType() {
        if (skinTypeCounts.isEmpty()) {
            Log.w(TAG, "No skin type counts available");
            return "Unknown";
        }
        int maxCount = 0;
        String mostCommonSkinType = "Unknown";
        for (Map.Entry<String, Integer> entry : skinTypeCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostCommonSkinType = entry.getKey();
            }
        }
        return mostCommonSkinType;
    }
}