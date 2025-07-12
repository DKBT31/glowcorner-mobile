package com.example.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobile.Api.ApiClient;
import com.example.mobile.Api.ApiResponse;
import com.example.mobile.Api.ApiService;
import com.example.mobile.Models.RegisterRequest;
import com.google.android.material.textfield.TextInputEditText;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = "SignUpActivity";

    private TextInputEditText fullNameEditText;
    private TextInputEditText emailEditText;
    private TextInputEditText phoneEditText;
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
        fullNameEditText = findViewById(R.id.fullNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
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
        String fullName = fullNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // Validation
        if (fullName.isEmpty()) {
            fullNameEditText.setError("Full name is required");
            fullNameEditText.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            phoneEditText.setError("Phone number is required");
            phoneEditText.requestFocus();
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

        // Disable button during signup attempt
        signUpButton.setText("Creating Account...");

        performSignUp(fullName, email, phone, password);
    }

    private void performSignUp(String fullName, String email, String phone, String password) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        RegisterRequest registerRequest = new RegisterRequest(fullName, email, phone, password);

        Log.d(TAG, "Starting registration for: " + email);

        // Use ResponseBody instead of ApiResponse to avoid parsing issues
        Call<ResponseBody> call = apiService.registerSimple(registerRequest);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                // Re-enable button
                signUpButton.setEnabled(true);
                signUpButton.setText("Create Account");

                Log.d(TAG, "Registration response code: " + response.code());

                if (response.isSuccessful()) {
                    // Registration successful - redirect to sign in
                    Toast.makeText(SignUpActivity.this,
                            "Account created successfully! Please sign in.",
                            Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(SignUpActivity.this,
                            "Registration failed: " + response.message(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Registration API call failed", t);

                signUpButton.setEnabled(true);
                signUpButton.setText("Create Account");

                Toast.makeText(SignUpActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleSuccessfulRegistration() {
        try {
            Log.d(TAG, "Handling successful registration");

            showToast("Account created successfully! Please sign in.");

            // Small delay to ensure toast is shown before navigation
            signUpButton.postDelayed(() -> {
                try {
                    Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    Log.d(TAG, "Navigation to SignInActivity completed");
                } catch (Exception e) {
                    Log.e(TAG, "Error during navigation", e);
                    showToast("Registration successful but navigation failed");
                }
            }, 500);

        } catch (Exception e) {
            Log.e(TAG, "Error in handleSuccessfulRegistration", e);
            showToast("Registration successful but error occurred");
        }
    }

    private void showToast(String message) {
        try {
            if (this != null && !isFinishing()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing toast", e);
        }
    }
}