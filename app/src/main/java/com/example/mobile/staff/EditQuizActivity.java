package com.example.mobile.staff;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobile.Api.ApiClient;
import com.example.mobile.Api.ApiService;
import com.example.mobile.R;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditQuizActivity extends AppCompatActivity {
    private static final String TAG = "EditQuizActivity";
    private EditText quizTextEdit;
    private LinearLayout optionsContainer;
    private Button addOptionButton, saveButton, cancelButton;
    private String questionId;

    private static class AnswerOption {
        String optionID;
        String optionText;
        String skinType;

        AnswerOption(String optionID, String optionText, String skinType) {
            this.optionID = optionID;
            this.optionText = optionText;
            this.skinType = skinType;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.staff_edit_quiz);

        quizTextEdit = findViewById(R.id.quizTextEdit);
        optionsContainer = findViewById(R.id.optionsContainer);
        addOptionButton = findViewById(R.id.addOptionButton);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);

        questionId = getIntent().getStringExtra("questionId");
        if (questionId == null) {
            Toast.makeText(this, "Invalid quiz ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadQuizData();
        setupButtons();
    }

    private void loadQuizData() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ResponseBody> call = apiService.getQuizById(questionId);
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
                            JsonObject data = parser.parse(responseString).getAsJsonObject().getAsJsonObject("data");
                            quizTextEdit.setText(data.get("quizText").getAsString());

                            JsonArray optionsArray = data.getAsJsonArray("answerOptionDTOS");
                            optionsContainer.removeAllViews();
                            for (JsonElement element : optionsArray) {
                                JsonObject optionObj = element.getAsJsonObject();
                                addOptionView(
                                        optionObj.get("optionID").getAsString(),
                                        optionObj.get("optionText").getAsString(),
                                        optionObj.get("skinType").getAsString()
                                );
                            }
                        } else {
                            Log.e(TAG, "API Error: " + jsonObject.getString("description"));
                            Toast.makeText(EditQuizActivity.this, "Failed to load quiz: " + jsonObject.getString("description"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "Error parsing quiz response: " + e.getMessage(), e);
                        Toast.makeText(EditQuizActivity.this, "Error reading quiz data", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Response not successful. Status: " + response.code());
                    Toast.makeText(EditQuizActivity.this, "Failed to load quiz. Status: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "API Call Failed for quiz: " + t.getMessage(), t);
                Toast.makeText(EditQuizActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupButtons() {
        addOptionButton.setOnClickListener(v -> addOptionView(null, "", "Dry"));
        saveButton.setOnClickListener(v -> saveQuiz());
        cancelButton.setOnClickListener(v -> finish());
    }

    private void addOptionView(String optionID, String optionText, String skinType) {
        View optionView = getLayoutInflater().inflate(R.layout.staff_item_edit_option, optionsContainer, false);
        EditText optionTextEdit = optionView.findViewById(R.id.optionTextEdit);
        Spinner skinTypeSpinner = optionView.findViewById(R.id.skinTypeSpinner);
        Button removeButton = optionView.findViewById(R.id.removeOptionButton);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.skin_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        skinTypeSpinner.setAdapter(adapter);
        skinTypeSpinner.setSelection(adapter.getPosition(skinType));

        optionTextEdit.setText(optionText);
        if (optionsContainer.getChildCount() > 1) {
            removeButton.setVisibility(View.VISIBLE);
            removeButton.setOnClickListener(v -> optionsContainer.removeView(optionView));
        }

        optionsContainer.addView(optionView);
    }

    private void saveQuiz() {
        List<AnswerOption> options = new ArrayList<>();
        for (int i = 0; i < optionsContainer.getChildCount(); i++) {
            View view = optionsContainer.getChildAt(i);
            EditText optionTextEdit = view.findViewById(R.id.optionTextEdit);
            Spinner skinTypeSpinner = view.findViewById(R.id.skinTypeSpinner);
            String optionText = optionTextEdit.getText().toString().trim();
            String skinType = skinTypeSpinner.getSelectedItem().toString();
            if (!optionText.isEmpty()) {
                options.add(new AnswerOption(null, optionText, skinType));
            }
        }

        if (quizTextEdit.getText().toString().trim().isEmpty() || options.isEmpty()) {
            Toast.makeText(this, "Please enter a question and at least one option", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject quizData = new JSONObject();
        try {
            quizData.put("questionId", questionId);
            quizData.put("quizText", quizTextEdit.getText().toString().trim());
            JSONArray optionsArray = new JSONArray();
            for (AnswerOption option : options) {
                JSONObject optionObj = new JSONObject();
                optionObj.put("optionText", option.optionText);
                optionObj.put("skinType", option.skinType);
                optionsArray.put(optionObj);
            }
            quizData.put("answerOptionDTOS", optionsArray);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON: " + e.getMessage(), e);
            Toast.makeText(this, "Error preparing data", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ResponseBody> call = apiService.updateQuiz(questionId, quizData.toString());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseString = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseString);
                        if (jsonObject.getBoolean("success")) {
                            Toast.makeText(EditQuizActivity.this, "Quiz updated successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Log.e(TAG, "API Error: " + jsonObject.getString("description"));
                            Toast.makeText(EditQuizActivity.this, "Failed to update quiz: " + jsonObject.getString("description"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "Error parsing response: " + e.getMessage(), e);
                        Toast.makeText(EditQuizActivity.this, "Error processing response", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Response not successful. Status: " + response.code());
                    Toast.makeText(EditQuizActivity.this, "Failed to update quiz. Status: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "API Call Failed for update: " + t.getMessage(), t);
                Toast.makeText(EditQuizActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}