package com.example.mobile.manager;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.mobile.Api.ApiClient;
import com.example.mobile.Api.ApiService;
import com.example.mobile.Models.Product;
import com.example.mobile.Models.ProductResponse;
import com.example.mobile.R;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProductActivity extends AppCompatActivity {
    private static final String TAG = "EditProductActivity";

    private TextView productIdTextView;
    private EditText productNameEditText, descriptionEditText, priceEditText, ratingEditText;
    private LinearLayout categoryContainer, skinTypesContainer;
    private ImageView productImageView;
    private Button saveButton, cancelButton, chooseImageButton;
    private ProgressDialog progressDialog;

    private String productID;
    private Product product;
    private Uri imageUri;
    private List<String> selectedCategories = new ArrayList<>();
    private List<String> selectedSkinTypes = new ArrayList<>();

    private final List<String> categories = Arrays.asList("Cleanser", "Toner", "Serum", "Moisturizer", "Sunscreen", "Mask");
    private final List<String> skinTypes = Arrays.asList("Dry", "Oily", "Combination", "Sensitive", "Normal");

    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manager_edit_product);

        // Initialize UI components
        productIdTextView = findViewById(R.id.product_id);
        productNameEditText = findViewById(R.id.product_name);
        descriptionEditText = findViewById(R.id.description);
        priceEditText = findViewById(R.id.price);
        ratingEditText = findViewById(R.id.rating);
        categoryContainer = findViewById(R.id.category_container);
        skinTypesContainer = findViewById(R.id.skin_types_container);
        productImageView = findViewById(R.id.product_image);
        saveButton = findViewById(R.id.save_button);
        cancelButton = findViewById(R.id.cancel_button);
        chooseImageButton = findViewById(R.id.choose_image_button);

        // Initialize ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        productID = getIntent().getStringExtra("productID");
        if (productID != null) {
            loadProductDetails(productID);
        }

        setupMultiSelect();
        setupButtons();
        setupImagePicker();
    }

    private void setupMultiSelect() {
        // Setup Categories
        for (String category : categories) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(category);
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!selectedCategories.contains(category)) selectedCategories.add(category);
                } else {
                    selectedCategories.remove(category);
                }
            });
            categoryContainer.addView(checkBox);
        }

        // Setup Skin Types
        for (String skinType : skinTypes) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(skinType);
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!selectedSkinTypes.contains(skinType)) selectedSkinTypes.add(skinType);
                } else {
                    selectedSkinTypes.remove(skinType);
                }
            });
            skinTypesContainer.addView(checkBox);
        }
    }

    private void setupButtons() {
        saveButton.setOnClickListener(v -> updateProduct());
        cancelButton.setOnClickListener(v -> finish());
        chooseImageButton.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
    }

    private void setupImagePicker() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                result -> {
                    if (result != null) {
                        imageUri = result;
                        Glide.with(this).load(imageUri).into(productImageView);
                    }
                }
        );
    }

    private void loadProductDetails(String productID) {
        progressDialog.show();
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ProductResponse> call = apiService.getFilterProductById(productID);

        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    product = response.body().getData().get(0);
                    populateFields();
                } else {
                    String errorMsg = response.body() != null ? response.body().getDescription() : response.message();
                    Log.e(TAG, "Load Response unsuccessful: " + errorMsg);
                    Toast.makeText(EditProductActivity.this, "Failed to load product: " + errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                progressDialog.dismiss();
                Log.e(TAG, "Load API Call Failed: " + t.getMessage(), t);
                Toast.makeText(EditProductActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateFields() {
        productIdTextView.setText(product.getProductID());
        productNameEditText.setText(product.getProductName());
        descriptionEditText.setText(product.getDescription());
        priceEditText.setText(String.valueOf(product.getPrice()));
        ratingEditText.setText(String.valueOf(product.getRating()));
        selectedCategories.clear();
        if (product.getCategory() != null) {
            selectedCategories.add(product.getCategory());
        }
        selectedSkinTypes.clear();
        selectedSkinTypes.addAll(product.getSkinTypes());
        updateCheckBoxes();
        if (!product.getImageUrl().isEmpty()) {
            Glide.with(this).load(product.getImageUrl()).into(productImageView);
        }
    }

    private void updateCheckBoxes() {
        for (int i = 0; i < categoryContainer.getChildCount(); i++) {
            View child = categoryContainer.getChildAt(i);
            if (child instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) child;
                checkBox.setChecked(selectedCategories.contains(checkBox.getText().toString()));
            }
        }
        for (int i = 0; i < skinTypesContainer.getChildCount(); i++) {
            View child = skinTypesContainer.getChildAt(i);
            if (child instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) child;
                checkBox.setChecked(selectedSkinTypes.contains(checkBox.getText().toString()));
            }
        }
    }

    private void updateProduct() {
        if (product == null) return;

        progressDialog.setMessage("Updating...");
        progressDialog.show();

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        // Create Product object without discountedPrice
        Product updatedProduct = new Product(
                product.getProductID(),
                productNameEditText.getText().toString(),
                descriptionEditText.getText().toString(),
                Float.parseFloat(priceEditText.getText().toString()),
                null, // discountedPrice set to null
                selectedSkinTypes,
                selectedCategories.isEmpty() ? null : selectedCategories.get(0),
                Float.parseFloat(ratingEditText.getText().toString()),
                product.getImageUrl(),
                product.getStatus()
        );

        // Convert Product to JsonObject
        JsonObject productJson = new Gson().toJsonTree(updatedProduct).getAsJsonObject();

        // Add product data as JsonObject
        builder.addFormDataPart("product", null, RequestBody.create(MediaType.parse("application/json"), productJson.toString()));

        // Add image if selected
        MultipartBody.Part imagePart = null;
        if (imageUri != null) {
            File file = new File(getRealPathFromURI(imageUri));
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            imagePart = MultipartBody.Part.createFormData("image", file.getName(), requestFile);
        }

        Call<ProductResponse> call = apiService.updateProduct(product.getProductID(), productJson, imagePart);
        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(EditProductActivity.this, "Product updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String errorMsg = response.body() != null ? response.body().getDescription() : response.message();
                    Log.e(TAG, "Update Response unsuccessful: " + errorMsg);
                    Toast.makeText(EditProductActivity.this, "Failed to update product: " + errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                progressDialog.dismiss();
                Log.e(TAG, "Update API Call Failed: " + t.getMessage(), t);
                Toast.makeText(EditProductActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getRealPathFromURI(Uri uri) {
        // Simplified implementation; use a proper URI to file path converter based on your needs
        return uri.getPath();
    }
}