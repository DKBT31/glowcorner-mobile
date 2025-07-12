package com.example.mobile.manager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile.Adapter.ProductAdapter;
import com.example.mobile.Api.ApiClient;
import com.example.mobile.Api.ApiService;
import com.example.mobile.Models.Product;
import com.example.mobile.Models.ProductResponse;
import com.example.mobile.Models.Promotion;
import com.example.mobile.R;
import com.example.mobile.SignInActivity;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditPromotionActivity extends AppCompatActivity {
    private static final String TAG = "EditPromotionActivity";
    private EditText etPromotionName, etDiscount, etStartDate, etEndDate;
    private TextView tvSelectedProducts;
    private Button btnSave, btnCancel, btnSelectProducts;
    private RecyclerView rvProducts;
    private ProductAdapter productAdapter;
    private List<Product> allProducts = new ArrayList<>();
    private List<Product> selectedProducts = new ArrayList<>();
    private Promotion promotion;
    private ApiService apiService;
    private String jwtToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate started");
        setContentView(R.layout.activity_edit_promotion);

        // Initialize views
        etPromotionName = findViewById(R.id.et_promotion_name);
        etDiscount = findViewById(R.id.et_discount);
        etStartDate = findViewById(R.id.et_start_date);
        etEndDate = findViewById(R.id.et_end_date);
        tvSelectedProducts = findViewById(R.id.tv_selected_products);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
        btnSelectProducts = findViewById(R.id.btn_select_products);
        rvProducts = findViewById(R.id.rv_products);
        Log.d(TAG, "UI components initialized");

        // Set up RecyclerView
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        productAdapter = new ProductAdapter(allProducts);
        rvProducts.setAdapter(productAdapter);
        Log.d(TAG, "RecyclerView set up");

        // Get promotion ID from intent
        String promotionId = getIntent().getStringExtra("promotionId");
        if (promotionId == null) {
            Log.e(TAG, "Promotion ID not found");
            Toast.makeText(this, "Promotion ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Log.d(TAG, "Promotion ID: " + promotionId);

        // Initialize API service
        jwtToken = SignInActivity.getStoredValue(this, "jwtToken");
        if (jwtToken == null) {
            Log.e(TAG, "JWT Token not found");
            Toast.makeText(this, "Please log in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Log.d(TAG, "JWT Token retrieved");
        apiService = ApiClient.getClient().create(ApiService.class);

        // Set click listener for Select Products
        btnSelectProducts.setOnClickListener(v -> {
            Log.d(TAG, "Select Products button clicked");
            if (allProducts.isEmpty()) {
                Toast.makeText(EditPromotionActivity.this, "No products available to select. Please wait for data to load.", Toast.LENGTH_SHORT).show();
            } else {
                showProductDialog();
            }
        });
        Log.d(TAG, "Click listener set");

        // Load data
        loadPromotionAndProducts(promotionId);
        Log.d(TAG, "onCreate finished");
    }

    private void loadPromotionAndProducts(String promotionId) {
        Log.d(TAG, "loadPromotionAndProducts started for ID: " + promotionId);
        setLoading(true);
        apiService.getPromotionById("Bearer " + jwtToken, promotionId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "getPromotionById response: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseString = response.body().string();
                        Log.d(TAG, "Promotion Response: " + responseString);
                        JsonParser parser = new JsonParser();
                        JsonObject rootObj = parser.parse(responseString).getAsJsonObject();
                        if (rootObj.get("success").getAsBoolean() && rootObj.get("status").getAsInt() == 200) {
                            JsonObject promoObj = rootObj.getAsJsonObject("data");
                            if (promoObj != null) {
                                List<String> productIDs = new ArrayList<>();
                                JsonArray productIdsArray = promoObj.getAsJsonArray("productIDs");
                                if (productIdsArray != null) {
                                    for (JsonElement id : productIdsArray) {
                                        productIDs.add(id.getAsString());
                                    }
                                }
                                promotion = new Promotion(
                                        promoObj.get("promotionID").getAsString(),
                                        promoObj.get("promotionName").getAsString(),
                                        promoObj.get("discount").getAsInt(),
                                        promoObj.get("startDate").getAsString(),
                                        promoObj.get("endDate").getAsString(),
                                        productIDs
                                );
                                updateUI();
                                Log.d(TAG, "Promotion loaded: " + promotion.getPromotionName());
                            } else {
                                Log.e(TAG, "Promotion data is null");
                                Toast.makeText(EditPromotionActivity.this, "No promotion data found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(TAG, "Failed to load promotion: " + rootObj.get("description").getAsString());
                            Toast.makeText(EditPromotionActivity.this, "No promotion found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error parsing promotion", e);
                        Toast.makeText(EditPromotionActivity.this, "Error loading promotion", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Failed to load promotion. Status: " + response.code());
                    Toast.makeText(EditPromotionActivity.this, "Failed to load promotion. Status: " + response.code(), Toast.LENGTH_SHORT).show();
                }
                setLoading(false);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "API Call Failed for getPromotionById", t);
                Toast.makeText(EditPromotionActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                setLoading(false);
            }
        });

        apiService.getProducts().enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                Log.d(TAG, "getProducts response: " + response.code());
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Product> products = response.body().getData();
                    if (products != null && !products.isEmpty()) {
                        allProducts.clear();
                        allProducts.addAll(products);
                        productAdapter.updateData(allProducts);
                        if (promotion != null) {
                            selectedProducts.clear();
                            selectedProducts.addAll(allProducts.stream()
                                    .filter(p -> promotion.getProductIDs().contains(p.getProductID()))
                                    .collect(Collectors.toList()));
                            updateSelectedProductsDisplay();
                        }
                        Log.d(TAG, "Products loaded: " + allProducts.size() + " items");
                    } else {
                        Log.w(TAG, "No products returned");
                        Toast.makeText(EditPromotionActivity.this, "No products available", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Failed to load products. Status: " + response.code());
                    Toast.makeText(EditPromotionActivity.this, "Failed to load products. Status: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                setLoading(false);
                Log.e(TAG, "API Call Failed for getProducts", t);
                Toast.makeText(EditPromotionActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI() {
        if (promotion != null) {
            etPromotionName.setText(promotion.getPromotionName());
            etDiscount.setText(String.valueOf(promotion.getDiscount()));
            etStartDate.setText(promotion.getStartDate());
            etEndDate.setText(promotion.getEndDate());
            updateSelectedProductsDisplay();
            Log.d(TAG, "UI updated with promotion: " + promotion.getPromotionName());
        }
    }

    private void updateSelectedProductsDisplay() {
        if (selectedProducts.isEmpty()) {
            tvSelectedProducts.setText("No products selected");
        } else {
            StringBuilder sb = new StringBuilder("Selected Products: ");
            for (int i = 0; i < selectedProducts.size(); i++) {
                sb.append(selectedProducts.get(i).getProductName()).append(" (ID: ").append(selectedProducts.get(i).getProductID()).append(")");
                if (i < selectedProducts.size() - 1) sb.append(", ");
            }
            tvSelectedProducts.setText(sb.toString());
            Log.d(TAG, "Selected products updated: " + selectedProducts.size() + " items");
        }
    }

    private void showProductDialog() {
        Log.d(TAG, "Showing ProductDialog with " + allProducts.size() + " products");
        ProductDialogFragment dialog = ProductDialogFragment.newInstance(allProducts, selectedProducts);
        dialog.setProductSelectionListener(selectedProductIDs -> {
            selectedProducts.clear();
            selectedProducts.addAll(allProducts.stream()
                    .filter(p -> selectedProductIDs.contains(p.getProductID()))
                    .collect(Collectors.toList()));
            if (promotion != null) {
                promotion.setProductIDs(new ArrayList<>(selectedProductIDs));
            }
            updateSelectedProductsDisplay();
            Log.d(TAG, "Product selection updated: " + selectedProductIDs.size() + " items");
        });
        try {
            dialog.show(getSupportFragmentManager(), "ProductDialog");
            Log.d(TAG, "ProductDialog shown successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to show ProductDialog: " + e.getMessage());
            Toast.makeText(EditPromotionActivity.this, "Failed to open product selection", Toast.LENGTH_SHORT).show();
        }
    }

    private void savePromotion() {
        if (promotion == null) return;

        promotion.setPromotionName(etPromotionName.getText().toString().trim());
        promotion.setDiscount(Integer.parseInt(etDiscount.getText().toString().trim()));
        promotion.setStartDate(etStartDate.getText().toString().trim());
        promotion.setEndDate(etEndDate.getText().toString().trim());

        if (selectedProducts.isEmpty()) {
            Toast.makeText(this, "Please select at least one product", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        apiService.updatePromotion("Bearer " + jwtToken, promotion.getPromotionId(), promotion).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseString = response.body().string();
                        Log.d(TAG, "Update Response: " + responseString);
                        JsonObject rootObj = new JsonParser().parse(responseString).getAsJsonObject();
                        if (rootObj.get("success").getAsBoolean() && rootObj.get("status").getAsInt() == 200) {
                            Toast.makeText(EditPromotionActivity.this, "Promotion updated successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(EditPromotionActivity.this, "Failed to update: " + rootObj.get("description").getAsString(), Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error parsing update response", e);
                        Toast.makeText(EditPromotionActivity.this, "Error updating promotion", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EditPromotionActivity.this, "Failed to update. Status: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                setLoading(false);
                Log.e(TAG, "API Call Failed", t);
                Toast.makeText(EditPromotionActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        btnSave.setEnabled(!isLoading);
        btnCancel.setEnabled(!isLoading);
        btnSelectProducts.setEnabled(!isLoading);
    }
}