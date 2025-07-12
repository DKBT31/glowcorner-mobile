package com.example.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.mobile.Api.ApiClient;
import com.example.mobile.Api.ApiService;
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

public class QuizActivity extends AppCompatActivity {
    private static final String TAG = "QuizActivity";
    private LinearLayout quizContainer;
    private ProgressBar progressBar;
    private TextView progressText;
    private Button nextButton;
    private LinearLayout dynamicContent; // Reference to dynamic_content

    private List<Quiz> quizzes = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private Map<String, Integer> skinTypeCounts = new HashMap<>();
    private List<String> selectedAnswers = new ArrayList<>();
    private static final int MAX_QUESTIONS = 5;

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
        setContentView(R.layout.activity_quiz);

        quizContainer = findViewById(R.id.quiz_container);
        progressBar = findViewById(R.id.progress_bar);
        progressText = findViewById(R.id.progress_text);
        nextButton = findViewById(R.id.next_button);
        dynamicContent = findViewById(R.id.dynamic_content); // Initialize dynamic_content

        Log.d(TAG, "onCreate: Initialized UI components");
        loadQuizzes();
        setupNextButton();
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
                            // Limit to first 5 quizzes
                            if (quizzes.size() > MAX_QUESTIONS) {
                                quizzes = quizzes.subList(0, MAX_QUESTIONS);
                            }
                            Log.d(TAG, "Loaded " + quizzes.size() + " quizzes");
                            displayQuestion();
                        } else {
                            Log.e(TAG, "API Error: " + jsonObject.getString("description"));
                            Toast.makeText(QuizActivity.this, "Failed to load quizzes: " + jsonObject.getString("description"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "Error parsing quiz response: " + e.getMessage(), e);
                        Toast.makeText(QuizActivity.this, "Error reading quiz data", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Response not successful. Status: " + response.code());
                    Toast.makeText(QuizActivity.this, "Failed to load quizzes. Status: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "API Call Failed for quizzes: " + t.getMessage(), t);
                Toast.makeText(QuizActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayQuestion() {
        dynamicContent.removeAllViews(); // Clear previous content
        if (quizzes.isEmpty() || currentQuestionIndex >= quizzes.size()) {
            Log.w(TAG, "No quizzes to display or index out of bounds: " + currentQuestionIndex);
            Toast.makeText(this, "No more questions", Toast.LENGTH_SHORT).show();
            return;
        }

        Quiz currentQuiz = quizzes.get(currentQuestionIndex);
        List<AnswerOption> currentOptions = currentQuiz.answerOptionDTOS;

        // Inflate card view
        CardView cardView = (CardView) getLayoutInflater().inflate(R.layout.card_quiz, dynamicContent, false);
        TextView quizTextView = cardView.findViewById(R.id.quizText);
        RadioGroup radioGroup = cardView.findViewById(R.id.answerOptions);

        quizTextView.setText(currentQuiz.quizText);
        Log.d(TAG, "Displaying question: " + currentQuiz.quizText + " with " + currentOptions.size() + " options");

        for (AnswerOption option : currentOptions) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setId(View.generateViewId());
            radioButton.setText(option.optionText);
            radioButton.setTextSize(16);
            radioButton.setTextColor(0xFF666666);
            radioGroup.addView(radioButton);
        }

        dynamicContent.addView(cardView); // Add to dynamic_content

        // Update progress
        int progress = (int) (((float) (currentQuestionIndex + 1) / MAX_QUESTIONS) * 100);
        progressBar.setProgress(progress);
        progressText.setText(String.format("Question %d of %d", currentQuestionIndex + 1, MAX_QUESTIONS));
        Log.d(TAG, "Progress updated to: " + progress + "%");

        nextButton.setEnabled(false); // Disable until an option is selected
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            nextButton.setEnabled(true);
            RadioButton selectedRadio = findViewById(checkedId);
            if (selectedRadio != null) {
                for (AnswerOption option : currentOptions) {
                    if (option.optionText.equals(selectedRadio.getText().toString())) {
                        selectedAnswers.add(option.optionText);
                        skinTypeCounts.put(option.skinType, skinTypeCounts.getOrDefault(option.skinType, 0) + 1);
                        Log.d(TAG, "Selected answer: " + option.optionText + " (Skin Type: " + option.skinType + ")");
                        break;
                    }
                }
            }
        });
    }

    private void setupNextButton() {
        nextButton = findViewById(R.id.next_button);
        nextButton.setOnClickListener(v -> handleNextQuestion());
        nextButton.setText("Next");
        nextButton.setEnabled(false); // Initially disabled
        Log.d(TAG, "Next button set up");
    }

    private void handleNextQuestion() {
        RadioGroup radioGroup = findViewById(R.id.answerOptions);
        int checkedId = radioGroup.getCheckedRadioButtonId();
        if (checkedId == -1) {
            Log.w(TAG, "No option selected");
            return;
        }

        if (currentQuestionIndex < MAX_QUESTIONS - 1) {
            currentQuestionIndex++;
            displayQuestion();
            nextButton.setEnabled(false); // Disable after moving to next question
            Log.d(TAG, "Moved to question " + (currentQuestionIndex + 1));
        } else {
            // Determine skin type after 5th question
            String determinedSkinType = determineSkinType();
            Log.d(TAG, "Determined skin type: " + determinedSkinType);
            Intent intent = new Intent(QuizActivity.this, ResultActivity.class);
            intent.putExtra("skinType", determinedSkinType);
            startActivity(intent);
            finish();
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