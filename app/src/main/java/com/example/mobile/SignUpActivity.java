package com.example.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobile.Api.ApiClient;
import com.example.mobile.Api.ApiService;
import com.example.mobile.Models.RegisterRequest;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = "SignUpActivity";

    private TextInputEditText usernameEditText;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private TextInputEditText confirmPasswordEditText;
    private Button signUpButton;
    private TextView signInLinkTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        usernameEditText = findViewById(R.id.usernameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        signUpButton = findViewById(R.id.signUpButton);
        signInLinkTextView = findViewById(R.id.signInLinkTextView);
    }

    private void setupClickListeners() {
        signUpButton.setOnClickListener(v -> attemptSignUp());

        signInLinkTextView.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void attemptSignUp() {
        String username = usernameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // Validation
        if (username.isEmpty()) {
            usernameEditText.setError("Username is required");
            usernameEditText.requestFocus();
            return;
        }

        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            usernameEditText.setError("Username can only contain letters, numbers, and underscores");
            usernameEditText.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Invalid email format");
            emailEditText.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            passwordEditText.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            confirmPasswordEditText.requestFocus();
            return;
        }

        signUpButton.setEnabled(false);
        signUpButton.setText("Creating Account...");
        performSignUp(username, email, password);
    }

    private void performSignUp(String username, String email, String password) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        RegisterRequest registerRequest = new RegisterRequest(username, email, password);

        Log.d(TAG, "Starting registration for: " + email);

        Call<ResponseBody> call = apiService.registerSimple(registerRequest);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                signUpButton.setEnabled(true);
                signUpButton.setText("Create Account");

                Log.d(TAG, "Registration response code: " + response.code());

                if (response.isSuccessful()) {
                    Toast.makeText(SignUpActivity.this,
                            "Account created successfully! Please sign in.",
                            Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    String errorMessage = "Registration failed";
                    try {
                        if (response.errorBody() != null) {
                            errorMessage = response.errorBody().string();
                        } else {
                            errorMessage = response.message();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error parsing error body", e);
                    }
                    Toast.makeText(SignUpActivity.this,
                            "Registration failed: " + errorMessage,
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Registration API call failed", t);
                signUpButton.setEnabled(true);
                signUpButton.setText("Create Account");
                Toast.makeText(SignUpActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}