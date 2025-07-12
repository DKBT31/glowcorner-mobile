package com.example.mobile;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.mobile.Api.ApiClient;
import com.example.mobile.Api.ApiService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {
    private static final String TAG = "EditProfileActivity";
    private static final int CAMERA_PERMISSION_REQUEST = 100;

    private EditText editFullName;
    private EditText editEmail;
    private EditText editPhone;
    private EditText editAddress;
    private AutoCompleteTextView spinnerSkinType;
    private Button saveButton;
    private Button cancelButton;
    private Button changePhotoButton;
    private ImageView profileImagePreview;

    // Store current user data for fields we don't edit
    private String currentSkinType;
    private String currentAvatarUrl;
    private Bitmap selectedImageBitmap;

    // Activity result launchers
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize activity result launchers
        initializeActivityLaunchers();

        // Initialize views
        editFullName = findViewById(R.id.edit_full_name);
        editEmail = findViewById(R.id.edit_email);
        editPhone = findViewById(R.id.edit_phone);
        editAddress = findViewById(R.id.edit_address);
        spinnerSkinType = findViewById(R.id.spinner_skin_type);
        saveButton = findViewById(R.id.btn_save_profile);
        cancelButton = findViewById(R.id.btn_cancel_profile);
        changePhotoButton = findViewById(R.id.btn_change_photo);
        profileImagePreview = findViewById(R.id.profile_image_preview);

        // Set up skin type dropdown
        setupSkinTypeSpinner();

        // Load current user data
        loadUserProfile();

        // Set up button listeners
        saveButton.setOnClickListener(v -> saveProfile());
        cancelButton.setOnClickListener(v -> finish());
        changePhotoButton.setOnClickListener(v -> showPhotoPickerDialog());
    }

    private void initializeActivityLaunchers() {
        // Camera launcher
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Bundle extras = result.getData().getExtras();
                            selectedImageBitmap = (Bitmap) extras.get("data");
                            profileImagePreview.setImageBitmap(selectedImageBitmap);
                        }
                    }
                });

        // Gallery launcher
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Uri imageUri = result.getData().getData();
                            try {
                                selectedImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                                profileImagePreview.setImageBitmap(selectedImageBitmap);
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(EditProfileActivity.this, "Error loading image", Toast.LENGTH_SHORT)
                                        .show();
                            }
                        }
                    }
                });
    }

    private void showPhotoPickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Photo");
        builder.setItems(new String[] { "Take Photo", "Choose from Gallery" }, (dialog, which) -> {
            if (which == 0) {
                checkCameraPermissionAndTakePhoto();
            } else {
                openGallery();
            }
        });
        builder.show();
    }

    private void checkCameraPermissionAndTakePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.CAMERA },
                    CAMERA_PERMISSION_REQUEST);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(takePictureIntent);
        } else {
            Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupSkinTypeSpinner() {
        // Define skin type options matching the backend enum
        String[] skinTypes = { "Dry", "Oily", "Combination", "Sensitive", "Normal" };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, skinTypes);
        spinnerSkinType.setAdapter(adapter);
    }

    private void loadUserProfile() {
        String userId = SignInActivity.getStoredValue(this, "userID");
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ResponseBody> call = apiService.getUserProfile(userId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseString = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseString);

                        if (jsonObject.getBoolean("success")) {
                            JSONObject data = jsonObject.getJSONObject("data");

                            // Populate form fields
                            editFullName.setText(data.optString("fullName", ""));
                            editEmail.setText(data.optString("email", ""));
                            editPhone.setText(data.optString("phone", ""));
                            editAddress.setText(data.optString("address", ""));

                            // Set skin type in spinner
                            String skinType = data.optString("skinType", "");
                            if (!skinType.isEmpty()) {
                                spinnerSkinType.setText(skinType, false);
                            }

                            // Store current skin type and avatar for update request
                            currentSkinType = data.optString("skinType", null);
                            currentAvatarUrl = data.optString("avatar_url", null);

                            // Load current profile image
                            loadProfileImage(currentAvatarUrl);
                        } else {
                            Toast.makeText(EditProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "Error parsing profile response", e);
                        Toast.makeText(EditProfileActivity.this, "Error loading profile", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EditProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "API call failed", t);
                Toast.makeText(EditProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProfileImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty() && profileImagePreview != null) {
            try {
                // Use Glide to load the image from URL (consistent with ProfileActivity)
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.ava)
                        .error(R.drawable.ava)
                        .into(profileImagePreview);

                Log.d(TAG, "Loading profile image from: " + imageUrl);
            } catch (Exception e) {
                Log.e(TAG, "Error loading profile image", e);
                // Set default image on error
                profileImagePreview.setImageResource(R.drawable.ava);
            }
        } else {
            // Set default image if no URL provided
            if (profileImagePreview != null) {
                profileImagePreview.setImageResource(R.drawable.ava);
            }
            Log.d(TAG, "No profile image URL provided, using default");
        }
    }

    private void saveProfile() {
        String userId = SignInActivity.getStoredValue(this, "userID");

        if (userId == null) {
            Toast.makeText(this, "Please log in to save profile", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get form data
        final String fullName = editFullName.getText().toString().trim();
        final String email = editEmail.getText().toString().trim();
        final String phone = editPhone.getText().toString().trim();
        final String address = editAddress.getText().toString().trim();
        String skinTypeRaw = spinnerSkinType.getText().toString().trim();

        // Validate required fields
        if (fullName.isEmpty()) {
            editFullName.setError("Full name is required");
            return;
        }
        if (email.isEmpty()) {
            editEmail.setError("Email is required");
            return;
        }

        // Create final skin type variable for use in inner class
        final String skinType = skinTypeRaw.isEmpty() ? "Normal" : skinTypeRaw;

        Log.d(TAG, "=== SENDING FORM DATA ===");
        Log.d(TAG, "fullName: " + fullName);
        Log.d(TAG, "email: " + email);
        Log.d(TAG, "phone: " + phone);
        Log.d(TAG, "address: " + address);
        Log.d(TAG, "skinType: " + skinType);
        Log.d(TAG, "=== END FORM DATA ===");

        // Disable save button during request
        saveButton.setEnabled(false);
        saveButton.setText("Saving...");

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        // Create multipart body for image if one was selected
        MultipartBody.Part imagePart = null;
        if (selectedImageBitmap != null) {
            // Convert bitmap to byte array
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
            byte[] byteArray = stream.toByteArray();

            // Create RequestBody from byte array
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), byteArray);
            imagePart = MultipartBody.Part.createFormData("avatar", "profile_image.jpg", requestFile);
        }

        Call<ResponseBody> call = apiService.updateUserProfile(userId, fullName, email, phone, address, skinType,
                imagePart);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                // Re-enable button
                saveButton.setEnabled(true);
                saveButton.setText("Save Changes");

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseString = response.body().string();
                        Log.d(TAG, "Update response: " + responseString);
                        JSONObject jsonObject = new JSONObject(responseString);

                        if (jsonObject.getBoolean("success")) {
                            // Update stored values in SharedPreferences
                            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("userFullName", fullName);
                            editor.putString("userEmail", email);
                            editor.putString("userPhone", phone);
                            editor.putString("userAddress", address);
                            if (!skinType.isEmpty()) {
                                editor.putString("userSkinType", skinType);
                            }
                            editor.apply();

                            Toast.makeText(EditProfileActivity.this, "Profile updated successfully!",
                                    Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK); // Indicate success to calling activity
                            finish();
                        } else {
                            String errorMsg = jsonObject.optString("description", "Unknown error");
                            Toast.makeText(EditProfileActivity.this, "Failed to update: " + errorMsg,
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "Error parsing update response", e);
                        Toast.makeText(EditProfileActivity.this, "Error updating profile", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Update failed with response code: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            String errorResponse = response.errorBody().string();
                            Log.e(TAG, "Error response: " + errorResponse);
                        } catch (IOException e) {
                            Log.e(TAG, "Error reading error response", e);
                        }
                    }
                    Toast.makeText(EditProfileActivity.this, "Failed to update profile. Code: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // Re-enable button
                saveButton.setEnabled(true);
                saveButton.setText("Save Changes");

                Log.e(TAG, "Update API call failed", t);
                Toast.makeText(EditProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
