package com.example.mobile;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile.Adapter.HomeProductAdapter;
import com.example.mobile.Api.ApiClient;
import com.example.mobile.Api.ApiService;
import com.example.mobile.Models.Product;
import com.example.mobile.Models.UserRoutineResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRoutineActivity extends AppCompatActivity {
    private static final String TAG = "UserRoutineActivity";
    private TextView routineNameTextView;
    private TextView routineDescriptionTextView;
    private RecyclerView cleanserRecyclerView, tonerRecyclerView, serumRecyclerView;
    private RecyclerView moisturizerRecyclerView, sunscreenRecyclerView, maskRecyclerView;
    private TextView cleanserHeader, tonerHeader, serumHeader;
    private TextView moisturizerHeader, sunscreenHeader, maskHeader;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_routine);

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

        // Setup RecyclerViews
        setupRecyclerView(cleanserRecyclerView);
        setupRecyclerView(tonerRecyclerView);
        setupRecyclerView(serumRecyclerView);
        setupRecyclerView(moisturizerRecyclerView);
        setupRecyclerView(sunscreenRecyclerView);
        setupRecyclerView(maskRecyclerView);

        // Get userID from SharedPreferences
        userId = SignInActivity.getStoredValue(this, "userID");
        if (userId == null) {
            Log.e(TAG, "User ID not found in SharedPreferences");
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "onCreate: Loading user routine for userId: " + userId);
        loadUserRoutine();
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new HomeProductAdapter(null));
    }

    private void loadUserRoutine() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<UserRoutineResponse> call = apiService.getUserById(userId);
        call.enqueue(new Callback<UserRoutineResponse>() {
            @Override
            public void onResponse(Call<UserRoutineResponse> call, Response<UserRoutineResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    UserRoutineResponse.UserData userData = response.body().getData();
                    if (userData == null || userData.getSkinCareRoutine() == null) {
                        Log.e(TAG, "User data or skinCareRoutine is null");
                        Toast.makeText(UserRoutineActivity.this, "No routine data found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    UserRoutineResponse.RoutineData routineData = userData.getSkinCareRoutine();
                    routineNameTextView.setText(routineData.getRoutineName() != null ? routineData.getRoutineName() : "N/A");
                    routineDescriptionTextView.setText(routineData.getRoutineDescription() != null ? routineData.getRoutineDescription() : "N/A");

                    // Group products by category
                    List<Product> products = routineData.getProducts() != null ? routineData.getProducts() : new ArrayList<>();
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

                    Log.d(TAG, "Loaded routine: " + routineData.getRoutineName() + " with " + products.size() + " products");
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
                    Toast.makeText(UserRoutineActivity.this, "Failed to load routine: " + errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserRoutineResponse> call, Throwable t) {
                Log.e(TAG, "API Call Failed: " + t.getMessage(), t);
                Toast.makeText(UserRoutineActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSection(TextView header, RecyclerView recyclerView, List<Product> products) {
        if (products != null && !products.isEmpty()) {
            header.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setAdapter(new HomeProductAdapter(products));
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