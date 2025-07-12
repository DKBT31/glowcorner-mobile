package com.example.mobile.manager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile.Adapter.PromotionAdapter;
import com.example.mobile.Api.ApiClient;
import com.example.mobile.Api.ApiService;
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

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManagerPromotionsActivity extends AppCompatActivity implements PromotionAdapter.OnPromotionActionListener {
    private static final String TAG = "ManagerPromotionsActivity";
    private RecyclerView recyclerViewPromotions;
    private PromotionAdapter promotionAdapter;
    private List<Promotion> promotionList = new ArrayList<>();
    private EditText etFilterPromotionName, etFilterPromotionId, etFilterProductId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate called");
        setContentView(R.layout.manager_promotions);

        // Initialize UI components
        recyclerViewPromotions = findViewById(R.id.recycler_view_promotions);
        Button btnCreatePromotion = findViewById(R.id.btn_create_promotion);
        Button btnApplyFilters = findViewById(R.id.btn_apply_filters);
        etFilterPromotionName = findViewById(R.id.et_filter_promotion_name);
        etFilterPromotionId = findViewById(R.id.et_filter_promotion_id);
        etFilterProductId = findViewById(R.id.et_filter_product_id);

        // Set up RecyclerView
        promotionAdapter = new PromotionAdapter(this, promotionList, this);
        recyclerViewPromotions.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPromotions.setAdapter(promotionAdapter);

        // Set click listener for Create New Promotion (placeholder)
        btnCreatePromotion.setOnClickListener(v -> Toast.makeText(this, "Create New Promotion clicked", Toast.LENGTH_SHORT).show());

        // Set click listener for Apply Filters
        btnApplyFilters.setOnClickListener(v -> applyFilters());

        // Load promotions
        loadPromotions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
        // Reload promotions when resuming to ensure fresh data
        loadPromotions();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause called");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy called");
    }

    private void loadPromotions() {
        String jwtToken = SignInActivity.getStoredValue(this, "jwtToken");
        if (jwtToken == null) {
            Toast.makeText(this, "Please log in to view promotions.", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ResponseBody> call = apiService.getPromotions("Bearer " + jwtToken);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseString = response.body().string();
                        Log.d(TAG, "Promotion Response: " + responseString);

                        JsonParser parser = new JsonParser();
                        JsonObject rootObj = parser.parse(responseString).getAsJsonObject();
                        JsonElement successElement = rootObj.get("success");
                        JsonElement statusElement = rootObj.get("status");
                        boolean success = successElement != null && !successElement.isJsonNull() && successElement.getAsBoolean();
                        int status = statusElement != null && !statusElement.isJsonNull() ? statusElement.getAsInt() : -1;

                        if (success && status == 200) {
                            JsonArray dataArray = rootObj.getAsJsonArray("data");
                            promotionList.clear();
                            for (JsonElement element : dataArray) {
                                JsonObject promotionObj = element.getAsJsonObject();
                                String promotionId = promotionObj.get("promotionID").getAsString();
                                String promotionName = promotionObj.get("promotionName").getAsString();
                                int discount = promotionObj.get("discount").getAsInt();
                                String startDate = promotionObj.get("startDate").getAsString();
                                String endDate = promotionObj.get("endDate").getAsString();
                                JsonArray productIdsArray = promotionObj.getAsJsonArray("productIDs");
                                List<String> productIDs = new ArrayList<>();
                                if (productIdsArray != null) {
                                    for (JsonElement id : productIdsArray) {
                                        productIDs.add(id.getAsString());
                                    }
                                }

                                promotionList.add(new Promotion(promotionId, promotionName, discount, startDate, endDate, productIDs));
                            }
                            promotionAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(ManagerPromotionsActivity.this, "Failed to load promotions: " + (rootObj.has("description") ? rootObj.get("description").getAsString() : "Unknown error"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading response: " + e.getMessage());
                        Toast.makeText(ManagerPromotionsActivity.this, "Error reading promotion data", Toast.LENGTH_SHORT).show();
                    } catch (IllegalStateException e) {
                        Log.e(TAG, "Invalid JSON format: " + e.getMessage());
                        Toast.makeText(ManagerPromotionsActivity.this, "Invalid server response", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ManagerPromotionsActivity.this, "Failed to load promotions. Status: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "API Call Failed for promotions: " + t.getMessage(), t);
                Toast.makeText(ManagerPromotionsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilters() {
        String jwtToken = SignInActivity.getStoredValue(this, "jwtToken");
        if (jwtToken == null) {
            Toast.makeText(this, "Please log in to apply filters.", Toast.LENGTH_SHORT).show();
            return;
        }

        String promotionName = etFilterPromotionName.getText().toString().trim();
        String promotionId = etFilterPromotionId.getText().toString().trim();
        String productId = etFilterProductId.getText().toString().trim();

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        if (!promotionId.isEmpty()) {
            Call<ResponseBody> call = apiService.getPromotionById("Bearer " + jwtToken, promotionId);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    handleFilterResponse(response, "ID filter applied");
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e(TAG, "API Call Failed for promotion by ID: " + t.getMessage(), t);
                    Toast.makeText(ManagerPromotionsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else if (!productId.isEmpty()) {
            Call<ResponseBody> call = apiService.getActivePromotionsByProduct("Bearer " + jwtToken, productId);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    handleFilterResponse(response, "Product ID filter applied");
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e(TAG, "API Call Failed for active promotions by product: " + t.getMessage(), t);
                    Toast.makeText(ManagerPromotionsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else if (!promotionName.isEmpty()) {
            Call<ResponseBody> call = apiService.searchPromotions("Bearer " + jwtToken, promotionName, "");
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    handleFilterResponse(response, "Name filter applied");
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e(TAG, "API Call Failed for promotion search: " + t.getMessage(), t);
                    Toast.makeText(ManagerPromotionsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            loadPromotions();
        }
    }

    private void handleFilterResponse(Response<ResponseBody> response, String filterType) {
        if (response.isSuccessful() && response.body() != null) {
            try {
                String responseString = response.body().string();
                Log.d(TAG, filterType + ": " + responseString);

                JsonParser parser = new JsonParser();
                JsonObject rootObj = parser.parse(responseString).getAsJsonObject();
                JsonElement successElement = rootObj.get("success");
                JsonElement statusElement = rootObj.get("status");
                boolean success = successElement != null && !successElement.isJsonNull() && successElement.getAsBoolean();
                int status = statusElement != null && !statusElement.isJsonNull() ? statusElement.getAsInt() : -1;

                if (success && status == 200) {
                    JsonArray dataArray = rootObj.getAsJsonArray("data");
                    promotionList.clear();
                    for (JsonElement element : dataArray) {
                        JsonObject promotionObj = element.getAsJsonObject();
                        String promotionId = promotionObj.get("promotionID").getAsString();
                        String promotionName = promotionObj.get("promotionName").getAsString();
                        int discount = promotionObj.get("discount").getAsInt();
                        String startDate = promotionObj.get("startDate").getAsString();
                        String endDate = promotionObj.get("endDate").getAsString();
                        JsonArray productIdsArray = promotionObj.getAsJsonArray("productIDs");
                        List<String> productIDs = new ArrayList<>();
                        if (productIdsArray != null) {
                            for (JsonElement id : productIdsArray) {
                                productIDs.add(id.getAsString());
                            }
                        }

                        promotionList.add(new Promotion(promotionId, promotionName, discount, startDate, endDate, productIDs));
                    }
                    promotionAdapter.notifyDataSetChanged();
                    Toast.makeText(ManagerPromotionsActivity.this, filterType + " successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ManagerPromotionsActivity.this, "Failed to apply " + filterType.toLowerCase() + ": " + (rootObj.has("description") ? rootObj.get("description").getAsString() : "Unknown error"), Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error reading response: " + e.getMessage());
                Toast.makeText(ManagerPromotionsActivity.this, "Error reading filter data", Toast.LENGTH_SHORT).show();
            } catch (IllegalStateException e) {
                Log.e(TAG, "Invalid JSON format: " + e.getMessage());
                Toast.makeText(ManagerPromotionsActivity.this, "Invalid server response", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ManagerPromotionsActivity.this, "Failed to apply filter. Status: " + response.code(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onUpdatePromotion(String promotionId) {
        Log.d(TAG, "Update promotion requested for ID: " + promotionId);
        Intent intent = new Intent(this, EditPromotionActivity.class);
        intent.putExtra("promotionId", promotionId);
        startActivity(intent);
    }

    @Override
    public void onDeletePromotion(String promotionId) {
        Log.d(TAG, "Delete promotion requested for ID: " + promotionId);
        String jwtToken = SignInActivity.getStoredValue(this, "jwtToken");
        if (jwtToken == null) {
            Toast.makeText(this, "Please log in to delete promotions.", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ResponseBody> call = apiService.deletePromotion("Bearer " + jwtToken, promotionId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseString = response.body().string();
                        Log.d(TAG, "Delete Response: " + responseString);
                        JsonParser parser = new JsonParser();
                        JsonObject rootObj = parser.parse(responseString).getAsJsonObject();
                        if (rootObj.get("success").getAsBoolean() && rootObj.get("status").getAsInt() == 200) {
                            // Remove the deleted promotion from the list
                            promotionList.removeIf(p -> p.getPromotionId().equals(promotionId));
                            promotionAdapter.notifyDataSetChanged();
                            Toast.makeText(ManagerPromotionsActivity.this, "Promotion deleted successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ManagerPromotionsActivity.this, "Failed to delete: " + rootObj.get("description").getAsString(), Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading delete response: " + e.getMessage());
                        Toast.makeText(ManagerPromotionsActivity.this, "Error processing delete response", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ManagerPromotionsActivity.this, "Failed to delete promotion. Status: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "API Call Failed for delete promotion: " + t.getMessage(), t);
                Toast.makeText(ManagerPromotionsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}