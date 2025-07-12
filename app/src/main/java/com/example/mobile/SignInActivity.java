package com.example.mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobile.Api.ApiClient;
import com.example.mobile.Api.ApiResponse;
import com.example.mobile.Api.ApiService;
import com.example.mobile.Models.LoginRequest;
import com.example.mobile.manager.ManagerProductsActivity;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignInActivity extends AppCompatActivity {
    private static final String TAG = "SignInActivity";

    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private Button signInButton;
    private TextView signUpLinkTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signInButton = findViewById(R.id.signInButton);
        signUpLinkTextView = findViewById(R.id.signUpLinkTextView);
    }

    private void setupClickListeners() {
        signInButton.setOnClickListener(v -> attemptSignIn());

        signUpLinkTextView.setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }

    private void attemptSignIn() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty()) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            return;
        }

        // Disable button during login attempt
        signInButton.setEnabled(false);
        signInButton.setText("Signing In...");

        performLogin(email, password);
    }

    private void performLogin(String email, String password) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        LoginRequest loginRequest = new LoginRequest(email, password);

        Call<ApiResponse> call = apiService.login(loginRequest);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                // Re-enable button
                signInButton.setEnabled(true);
                signInButton.setText("Sign In");

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        handleSuccessfulLogin(apiResponse.getData());
                    } else {
                        Toast.makeText(SignInActivity.this,
                                "Login failed: " + apiResponse.getDescription(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SignInActivity.this,
                            "Login failed. Please check your credentials.",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                // Re-enable button
                signInButton.setEnabled(true);
                signInButton.setText("Sign In");

                Log.e(TAG, "Login API call failed: " + t.getMessage(), t);
                Toast.makeText(SignInActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleSuccessfulLogin(ApiResponse.Data data) {
        if (data != null) {
            // Store user data in SharedPreferences
            storeUserData(data);

            Toast.makeText(this, "Welcome, " + data.getFullName() + "!", Toast.LENGTH_SHORT).show();

            // Navigate based on user role
            Intent intent;
            String role = data.getRole();
            if ("STAFF".equals(role) || "MANAGER".equals(role)) {
                intent = new Intent(this, ManagerProductsActivity.class);
            } else {
                intent = new Intent(this, HomeActivity.class);
            }

            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Log.e(TAG, "Login data is null");
            Toast.makeText(this, "Login successful but error reading user data", Toast.LENGTH_SHORT).show();
        }
    }

    private void storeUserData(ApiResponse.Data data) {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("userID", data.getUserID());
        editor.putString("userFullName", data.getFullName());
        editor.putString("userEmail", data.getEmail());

        // Only update these fields if they don't already exist or are empty
        // This prevents overwriting existing user data like address
        if (getStoredValue(this, "userPhone") == null || getStoredValue(this, "userPhone").isEmpty()) {
            editor.putString("userPhone", ""); // Set default or get from data if available
        }
        if (getStoredValue(this, "userAddress") == null || getStoredValue(this, "userAddress").isEmpty()) {
            editor.putString("userAddress", ""); // Set default or get from data if available
        }
        if (getStoredValue(this, "userSkinType") == null || getStoredValue(this, "userSkinType").isEmpty()) {
            editor.putString("userSkinType", ""); // Set default or get from data if available
        }
        if (getStoredValue(this, "userAvatarUrl") == null || getStoredValue(this, "userAvatarUrl").isEmpty()) {
            editor.putString("userAvatarUrl", ""); // Set default or get from data if available
        }

        editor.putString("userRole", data.getRole());
        editor.putString("jwtToken", data.getJwtToken());

        editor.apply();
    }

    // Static utility methods
    public static String getStoredValue(android.content.Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", context.MODE_PRIVATE);
        return prefs.getString(key, null);
    }

    public static void clearStoredValues(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

    public static void storeValue(android.content.Context context, String key, String value) {
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }
}