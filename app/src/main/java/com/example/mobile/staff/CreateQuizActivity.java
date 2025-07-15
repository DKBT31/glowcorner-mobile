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

public class CreateQuizActivity extends AppCompatActivity {
    private static final String TAG = "CreateQuizActivity";
    private EditText quizTextEdit;
    private LinearLayout optionsContainer;
    private Button addOptionButton, createButton, cancelButton;

    private static class AnswerOption {
        String optionText;
        String skinType;

        AnswerOption(String optionText, String skinType) {
            this.optionText = optionText;
            this.skinType = skinType;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.staff_create_quiz); // Updated layout name to match previous context

        quizTextEdit = findViewById(R.id.quizTextEdit);
        optionsContainer = findViewById(R.id.optionsContainer);
        addOptionButton = findViewById(R.id.addOptionButton);
        createButton = findViewById(R.id.createButton);
        cancelButton = findViewById(R.id.cancelButton);

        // Initialize with one default option
        addOptionView("", "Dry");

        setupButtons();
    }

    private void setupButtons() {
        addOptionButton.setOnClickListener(v -> addOptionView("", "Dry"));
        createButton.setOnClickListener(v -> createQuiz());
        cancelButton.setOnClickListener(v -> finish());
    }

    private void addOptionView(String optionText, String skinType) {
        View optionView = getLayoutInflater().inflate(R.layout.staff_item_create_option, optionsContainer, false);
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

    private void createQuiz() {
        List<AnswerOption> options = new ArrayList<>();
        for (int i = 0; i < optionsContainer.getChildCount(); i++) {
            View view = optionsContainer.getChildAt(i);
            EditText optionTextEdit = view.findViewById(R.id.optionTextEdit);
            Spinner skinTypeSpinner = view.findViewById(R.id.skinTypeSpinner);
            String optionText = optionTextEdit.getText().toString().trim();
            String skinType = skinTypeSpinner.getSelectedItem().toString();
            if (!optionText.isEmpty()) {
                options.add(new AnswerOption(optionText, skinType));
            }
        }

        if (quizTextEdit.getText().toString().trim().isEmpty() || options.isEmpty()) {
            Toast.makeText(this, "Please enter a question and at least one option", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject quizData = new JSONObject();
        try {
            quizData.put("quizText", quizTextEdit.getText().toString().trim());
            JSONArray optionsArray = new JSONArray();
            for (AnswerOption option : options) {
                JSONObject optionObj = new JSONObject();
                optionObj.put("optionText", option.optionText);
                optionObj.put("skinType", option.skinType);
                optionsArray.put(optionObj);
            }
            quizData.put("answerOptionRequests", optionsArray);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON: " + e.getMessage(), e);
            Toast.makeText(this, "Error preparing data", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ResponseBody> call = apiService.createQuiz(quizData.toString());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseString = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseString);
                        if (jsonObject.getBoolean("success")) {
                            Toast.makeText(CreateQuizActivity.this, "Quiz created successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Log.e(TAG, "API Error: " + jsonObject.getString("description"));
                            Toast.makeText(CreateQuizActivity.this, "Failed to create quiz: " + jsonObject.getString("description"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "Error parsing response: " + e.getMessage(), e);
                        Toast.makeText(CreateQuizActivity.this, "Error processing response", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Response not successful. Status: " + response.code());
                    Toast.makeText(CreateQuizActivity.this, "Failed to create quiz. Status: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "API Call Failed for create: " + t.getMessage(), t);
                Toast.makeText(CreateQuizActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}