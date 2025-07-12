package com.example.mobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile.Adapter.ProductAdapter;
import com.example.mobile.Api.ApiClient;
import com.example.mobile.Api.ApiService;
import com.example.mobile.Models.Product;
import com.example.mobile.Models.RoutineResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoutineDetailActivity extends AppCompatActivity {
    private static final String TAG = "RoutineDetailActivity";
    private TextView routineNameTextView;
    private TextView routineDescriptionTextView;
    private RecyclerView cleanserRecyclerView, tonerRecyclerView, serumRecyclerView;
    private RecyclerView moisturizerRecyclerView, sunscreenRecyclerView, maskRecyclerView;
    private TextView cleanserHeader, tonerHeader, serumHeader;
    private TextView moisturizerHeader, sunscreenHeader, maskHeader;
    private Button applyButton;
    private String routineId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine_detail);

        // Get routineID from Intent or SharedPreferences
        routineId = getIntent().getStringExtra("routineID");
        if (routineId == null) {
            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            routineId = prefs.getString("routineID", null);
            if (routineId == null) {
                Log.e(TAG, "Routine ID not provided in Intent or SharedPreferences");
                Toast.makeText(this, "Routine ID not provided", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }

        // Initialize UI components
        routineNameTextView = findViewById(R.id.routine_name);
        routineDescriptionTextView = findViewById(R.id.routine_description);
        cleanserRecyclerView = findViewById(R.id.cleanser_recycler_view);
        tonerRecyclerView = findViewById(R.id.toner_recycler_view);
        serumRecyclerView = findViewById(R.id.serum_recycler_view);
        moisturizerRecyclerView = findViewById(R.id.moisturizer_recycler_view);
        sunscreenRecyclerView = findViewById(R.id.sunscreen_recycler_view);
        maskRecyclerView = findViewById(R.id.mask_recycler_view);
        cleanserHeader = findViewById(R.id.cleanser_header);
        tonerHeader = findViewById(R.id.toner_header);
        serumHeader = findViewById(R.id.serum_header);
        moisturizerHeader = findViewById(R.id.moisturizer_header);
        sunscreenHeader = findViewById(R.id.sunscreen_header);
        maskHeader = findViewById(R.id.mask_header);
        applyButton = findViewById(R.id.apply_button);

        // Setup RecyclerViews
        setupRecyclerView(cleanserRecyclerView);
        setupRecyclerView(tonerRecyclerView);
        setupRecyclerView(serumRecyclerView);
        setupRecyclerView(moisturizerRecyclerView);
        setupRecyclerView(sunscreenRecyclerView);
        setupRecyclerView(maskRecyclerView);

        // Setup Apply Button click listener
        applyButton.setOnClickListener(v -> applyRoutine());

        Log.d(TAG, "onCreate: Loading routine details for routineId: " + routineId);
        loadRoutineDetails();
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ProductAdapter(null));
    }

    private static final String PREF_NAME = "AppPrefs"; // Đảm bảo trùng với tên khi lưu

    private void applyRoutine() {
        String userID = SignInActivity.getStoredValue(this, "userID");

        if (userID == null) {
            Log.e(TAG, "User ID not found in SharedPreferences");
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<RoutineResponse> call = apiService.applyRoutineToUser(routineId, userID);
        call.enqueue(new Callback<RoutineResponse>() {
            @Override
            public void onResponse(Call<RoutineResponse> call, Response<RoutineResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(RoutineDetailActivity.this, "Routine applied successfully", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Routine applied successfully for userId: " + userID + ", routineId: " + routineId);
                } else {
                    String errorMsg = response.body() != null ? response.body().getDescription() : response.message();
                    Log.e(TAG, "Failed to apply routine: " + errorMsg);
                    if (response.errorBody() != null) {
                        try {
                            Log.e(TAG, "Error body: " + response.errorBody().string());
                        } catch (IOException e) {
                            Log.e(TAG, "Error reading error body: " + e.getMessage());
                        }
                    }
                    Toast.makeText(RoutineDetailActivity.this, "Failed to apply routine: " + errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RoutineResponse> call, Throwable t) {
                Log.e(TAG, "Apply routine API call failed: " + t.getMessage(), t);
                Toast.makeText(RoutineDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loadRoutineDetails() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<RoutineResponse> call = apiService.getRoutineById(routineId);
        call.enqueue(new Callback<RoutineResponse>() {
            @Override
            public void onResponse(Call<RoutineResponse> call, Response<RoutineResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    RoutineResponse.RoutineData data = response.body().getData();
                    if (data == null) {
                        Log.e(TAG, "Routine data is null");
                        Toast.makeText(RoutineDetailActivity.this, "No routine data found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    routineNameTextView.setText(data.getRoutineName() != null ? data.getRoutineName() : "N/A");
                    routineDescriptionTextView.setText(data.getRoutineDescription() != null ? data.getRoutineDescription() : "N/A");

                    // Group products by category
                    List<Product> products = data.getProductDTOS() != null ? data.getProductDTOS() : new ArrayList<>();
                    List<Product> cleansers = products.stream()
                            .filter(p -> "Cleanser".equalsIgnoreCase(p.getCategory()))
                            .collect(Collectors.toList());
                    List<Product> toners = products.stream()
                            .filter(p -> "Toner".equalsIgnoreCase(p.getCategory()))
                            .collect(Collectors.toList());
                    List<Product> serums = products.stream()
                            .filter(p -> "Serum".equalsIgnoreCase(p.getCategory()))
                            .collect(Collectors.toList());
                    List<Product> moisturizers = products.stream()
                            .filter(p -> "Moisturizer".equalsIgnoreCase(p.getCategory()))
                            .collect(Collectors.toList());
                    List<Product> sunscreens = products.stream()
                            .filter(p -> "Sunscreen".equalsIgnoreCase(p.getCategory()))
                            .collect(Collectors.toList());
                    List<Product> masks = products.stream()
                            .filter(p -> "Mask".equalsIgnoreCase(p.getCategory()))
                            .collect(Collectors.toList());

                    // Update RecyclerViews and headers
                    updateSection(cleanserHeader, cleanserRecyclerView, cleansers);
                    updateSection(tonerHeader, tonerRecyclerView, toners);
                    updateSection(serumHeader, serumRecyclerView, serums);
                    updateSection(moisturizerHeader, moisturizerRecyclerView, moisturizers);
                    updateSection(sunscreenHeader, sunscreenRecyclerView, sunscreens);
                    updateSection(maskHeader, maskRecyclerView, masks);

                    Log.d(TAG, "Loaded routine: " + data.getRoutineName() + " with " + products.size() + " products");
                } else {
                    String errorMsg = response.body() != null ? response.body().getDescription() : response.message();
                    Log.e(TAG, "Response unsuccessful: " + errorMsg);
                    if (response.errorBody() != null) {
                        try {
                            Log.e(TAG, "Error body: " + response.errorBody().string());
                        } catch (IOException e) {
                            Log.e(TAG, "Error reading error body: " + e.getMessage());
                        }
                    }
                    Toast.makeText(RoutineDetailActivity.this, "Failed to load routine: " + errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RoutineResponse> call, Throwable t) {
                Log.e(TAG, "API Call Failed: " + t.getMessage(), t);
                Toast.makeText(RoutineDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSection(TextView header, RecyclerView recyclerView, List<Product> products) {
        if (products != null && !products.isEmpty()) {
            header.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setAdapter(new ProductAdapter(products));
        } else {
            header.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Activity destroyed");
    }
}