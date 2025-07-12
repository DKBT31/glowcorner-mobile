package com.example.mobile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile.Api.ApiClient;
import com.example.mobile.Api.ApiService;
import com.example.mobile.Adapter.RoutineAdapter;
import com.example.mobile.Models.Routine;
import com.example.mobile.Models.Product;
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

public class ResultActivity extends AppCompatActivity {
    private static final String TAG = "ResultActivity";
    private TextView skinTypeText;
    private RecyclerView routinesRecyclerView;
    private RoutineAdapter routineAdapter;
    private String skinType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        skinType = getIntent().getStringExtra("skinType");
        if (skinType == null) {
            Toast.makeText(this, "Skin type not provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        skinTypeText = findViewById(R.id.skin_type_text);
        routinesRecyclerView = findViewById(R.id.routines_recycler_view);
        routinesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        routineAdapter = new RoutineAdapter(this, routine -> {
            // Save routineID to SharedPreferences
            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("routineID", routine.getRoutineID());
            editor.apply();

            // Navigate to RoutineDetailActivity
            Intent intent = new Intent(ResultActivity.this, RoutineDetailActivity.class);
            intent.putExtra("routineID", routine.getRoutineID());
            startActivity(intent);
        });
        routinesRecyclerView.setAdapter(routineAdapter);

        skinTypeText.setText("Your Skin Type: " + skinType);
        loadRoutines();
        updateCustomerSkinType();
    }

    private void loadRoutines() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ResponseBody> call = apiService.getSkinCareRoutines();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseString = response.body().string();
                        Log.d(TAG, "Routines Response: " + responseString);

                        JsonArray jsonArray = new JsonParser().parse(responseString).getAsJsonArray();
                        List<Routine> routines = new ArrayList<>();
                        for (JsonElement element : jsonArray) {
                            JsonObject routineObj = element.getAsJsonObject();
                            String routineId = routineObj.get("routineID").getAsString();
                            String skinType = routineObj.get("skinType").getAsString();
                            String routineName = routineObj.get("routineName").getAsString();
                            String routineDescription = routineObj.get("routineDescription").getAsString();
                            JsonArray productArray = routineObj.getAsJsonArray("productDTOS");
                            List<Product> products = new ArrayList<>();
                            for (JsonElement productElement : productArray) {
                                JsonObject productObj = productElement.getAsJsonObject();
                                products.add(new Product(
                                        productObj.get("productID").getAsString(),
                                        productObj.get("productName").getAsString(),
                                        productObj.get("description").getAsString(),
                                        productObj.get("price").getAsDouble(),
                                        productObj.get("image_url").getAsString(),
                                        productObj.get("category").getAsString(),
                                        productObj.get("rating").getAsDouble()
                                ));
                            }
                            if (skinType.equals(ResultActivity.this.skinType)) {
                                routines.add(new Routine(routineId, skinType, routineName, routineDescription, products));
                            }
                        }
                        routineAdapter.setRoutines(routines);
                    } catch (IOException e) {
                        Log.e(TAG, "Error parsing routines response: " + e.getMessage());
                        Toast.makeText(ResultActivity.this, "Error loading routines", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ResultActivity.this, "Failed to load routines. Status: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "API Call Failed for routines: " + t.getMessage(), t);
                Toast.makeText(ResultActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCustomerSkinType() {
        String userId = SignInActivity.getStoredValue(this, "userID");
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
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
                        Log.d(TAG, "Customer Response: " + responseString);

                        JsonObject jsonObject = new JsonParser().parse(responseString).getAsJsonObject();
                        if (jsonObject.get("success").getAsBoolean() && jsonObject.get("status").getAsInt() == 200) {
                            JsonObject dataObject = jsonObject.getAsJsonObject("data");
                            String currentSkinType = dataObject.get("skinType").getAsString();

                            if (!currentSkinType.equals(skinType)) {
                                // Placeholder for update API call
                                Toast.makeText(ResultActivity.this, "Skin type updated to " + skinType, Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error parsing customer response: " + e.getMessage());
                        Toast.makeText(ResultActivity.this, "Error updating skin type", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "API Call Failed for customer: " + t.getMessage(), t);
                Toast.makeText(ResultActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}