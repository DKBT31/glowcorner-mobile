package com.example.mobile.manager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobile.Api.ApiClient;
import com.example.mobile.Api.ApiService;
import com.example.mobile.Models.User;
import com.example.mobile.R;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManagerEditUserActivity extends AppCompatActivity {
    private static final String TAG = "ManagerEditUserActivity";
    private ProgressDialog progressDialog;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manager_edit_user);

        EditText userIdEditText = findViewById(R.id.edit_user_id);
        EditText userNameEditText = findViewById(R.id.edit_user_name);
        EditText userEmailEditText = findViewById(R.id.edit_user_email);
        EditText userPhoneEditText = findViewById(R.id.edit_user_phone);
        EditText userAddressEditText = findViewById(R.id.edit_user_address);
        EditText loyalPointsEditText = findViewById(R.id.edit_user_loyal_points);
        Spinner skinTypeSpinner = findViewById(R.id.edit_user_skin_type);
        Spinner userRoleSpinner = findViewById(R.id.edit_user_role);
        Button saveButton = findViewById(R.id.save_button);
        Button cancelButton = findViewById(R.id.cancel_button);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        // Set up spinners
        ArrayAdapter<CharSequence> skinTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.skin_types, android.R.layout.simple_spinner_item);
        skinTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        skinTypeSpinner.setAdapter(skinTypeAdapter);

        ArrayAdapter<CharSequence> roleAdapter = ArrayAdapter.createFromResource(this,
                R.array.user_roles, android.R.layout.simple_spinner_item);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userRoleSpinner.setAdapter(roleAdapter);

        String userId = getIntent().getStringExtra("userID");
        Log.d(TAG, "Received userID: " + userId); // Debug log for userID
        if (userId != null) {
            userIdEditText.setText(userId);
            loadUserProfile(userId, userNameEditText, userEmailEditText, userPhoneEditText,
                    userAddressEditText, loyalPointsEditText, skinTypeSpinner, userRoleSpinner);
        } else {
            Log.e(TAG, "userID is null, cannot load profile");
            Toast.makeText(this, "User ID not provided", Toast.LENGTH_SHORT).show();
        }

        saveButton.setOnClickListener(v -> {
            // Implement save logic here (e.g., API call to update user)
            Toast.makeText(this, "Save functionality to be implemented", Toast.LENGTH_SHORT).show();
        });

        cancelButton.setOnClickListener(v -> finish());
    }

    private void loadUserProfile(String userId, EditText nameEditText, EditText emailEditText,
                                 EditText phoneEditText, EditText addressEditText, EditText loyalPointsEditText,
                                 Spinner skinTypeSpinner, Spinner roleSpinner) {
        Log.d(TAG, "Loading profile for userID: " + userId); // Debug log
        progressDialog.show();
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ResponseBody> call = apiService.getUserProfile(userId);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressDialog.dismiss();
                Log.d(TAG, "API Response: " + response.code() + " - " + response.message()); // Debug log
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonString = response.body().string();
                        Log.d(TAG, "Raw JSON Response: " + jsonString); // Debug raw response

                        // Try parsing as a direct User object
                        User user = gson.fromJson(jsonString, User.class);
                        if (user == null) {
                            // If direct parsing fails, try wrapped in "data" field
                            JSONObject jsonObject = new JSONObject(jsonString);
                            user = gson.fromJson(jsonObject.getJSONObject("data").toString(), User.class);
                        }

                        if (user != null) {
                            Log.d(TAG, "Parsed User: " + gson.toJson(user)); // Log parsed user
                            nameEditText.setText(user.getFullName() != null ? user.getFullName() : "");
                            emailEditText.setText(user.getEmail() != null ? user.getEmail() : "");
                            phoneEditText.setText(user.getPhone() != null ? user.getPhone() : "");
                            addressEditText.setText(user.getAddress() != null ? user.getAddress() : "");
                            loyalPointsEditText.setText(String.valueOf(user.getLoyalPoints()));
                            if (user.getSkinType() != null) {
                                int skinTypePosition = ((ArrayAdapter<String>) skinTypeSpinner.getAdapter())
                                        .getPosition(user.getSkinType());
                                skinTypeSpinner.setSelection(skinTypePosition >= 0 ? skinTypePosition : 0);
                                Log.d(TAG, "Set skinType: " + user.getSkinType());
                            }
                            if (user.getRole() != null) {
                                int rolePosition = ((ArrayAdapter<String>) roleSpinner.getAdapter())
                                        .getPosition(user.getRole());
                                roleSpinner.setSelection(rolePosition >= 0 ? rolePosition : 0);
                                Log.d(TAG, "Set role: " + user.getRole());
                            }
                            Log.d(TAG, "Profile loaded successfully for userID: " + userId);
                        } else {
                            Log.e(TAG, "Failed to parse User from JSON");
                            Toast.makeText(ManagerEditUserActivity.this, "Failed to parse user data", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading response: " + e.getMessage(), e);
                        Toast.makeText(ManagerEditUserActivity.this, "Error reading data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing JSON: " + e.getMessage(), e);
                        Toast.makeText(ManagerEditUserActivity.this, "Error parsing data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorMsg = response.message();
                    Log.e(TAG, "Failed to load user profile: " + errorMsg);
                    Toast.makeText(ManagerEditUserActivity.this, "Error: " + errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.dismiss();
                Log.e(TAG, "API Call Failed: " + t.getMessage(), t);
                Toast.makeText(ManagerEditUserActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}