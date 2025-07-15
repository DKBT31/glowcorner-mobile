package com.example.mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mobile.Adapter.HomeProductAdapter;
import com.example.mobile.Api.ApiClient;
import com.example.mobile.Api.ApiService;
import com.example.mobile.Models.Product;
import com.example.mobile.Models.ProductResponse;

import java.util.Arrays;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";

    private RecyclerView recyclerView;
    private HomeProductAdapter adapter;
    private TextView userIdTextView;
    private Spinner skinTypeSpinner;
    private Spinner categorySpinner;
    private EditText searchEditText;
    private Button searchButton;
    private ImageView userProfileImageView;

    // Predefined lists for dropdowns
    private final List<String> skinTypes = Arrays.asList("All", "Dry", "Oily", "Combination", "Sensitive");
    private final List<String> categories = Arrays.asList("All", "Cleanser", "Serum", "Moisturizer");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize UI
        initViews();

        // Setup navigation and dropdown
        setupNavigation();

        // Setup spinners
        setupSpinners();

        // Setup search button
        setupSearch();

        // Load all products initially
        loadProducts();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view);
        userIdTextView = findViewById(R.id.userIdTextView);
        skinTypeSpinner = findViewById(R.id.skin_type_spinner);
        categorySpinner = findViewById(R.id.category_spinner);
        searchEditText = findViewById(R.id.search_edit_text);
        searchButton = findViewById(R.id.search_button);

        // Initialize the user profile button (now an ImageButton)
        ImageButton userProfileButton = findViewById(R.id.user_profile_button);
        ImageButton menuButton = findViewById(R.id.menu_button);

        // Use GridLayoutManager for better product display
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns
        adapter = new HomeProductAdapter(null);
        recyclerView.setAdapter(adapter);

        // Display user ID from SharedPreferences
        String userID = SignInActivity.getStoredValue(this, "userID");
        userIdTextView.setText(userID != null ? "User ID: " + userID : "User ID: Not logged in");

        // Load user profile image
        loadUserProfileImage();

        // Set click listeners for both buttons
        userProfileButton.setOnClickListener(v -> openUserProfileMenu());
        menuButton.setOnClickListener(v -> openMainMenu());
    }

    private void loadUserProfileImage() {
        String userID = SignInActivity.getStoredValue(this, "userID");
        if (userID == null) return;

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ResponseBody> call = apiService.getUserProfile(userID);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseString = response.body().string();
                        org.json.JSONObject jsonObject = new org.json.JSONObject(responseString);
                        org.json.JSONObject userData = jsonObject.getJSONObject("data");

                        String avatarUrl = userData.optString("avatar_url", "");
                        ImageButton userProfileButton = findViewById(R.id.user_profile_button);

                        if (!avatarUrl.isEmpty()) {
                            Glide.with(HomeActivity.this)
                                    .load(avatarUrl)
                                    .placeholder(R.drawable.rounded_background)
                                    .error(R.drawable.rounded_background)
                                    .circleCrop() // Makes the image circular
                                    .into(userProfileButton);
                        } else {
                            // Use default placeholder
                            userProfileButton.setImageResource(R.drawable.rounded_background);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error loading user profile image: " + e.getMessage());
                    ImageButton userProfileButton = findViewById(R.id.user_profile_button);
                    userProfileButton.setImageResource(R.drawable.rounded_background);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Failed to load user profile: " + t.getMessage());
                ImageButton userProfileButton = findViewById(R.id.user_profile_button);
                userProfileButton.setImageResource(R.drawable.rounded_background);
            }
        });
    }

    private void setupNavigation() {
        NavigationManager.setupNavigation(this, findViewById(R.id.bottom_navigation));
        new DropdownMenuManager(this);
    }

    private void openUserProfileMenu() {
        // Create a popup menu or intent to navigate to profile-related options
        PopupMenu popup = new PopupMenu(this, findViewById(R.id.user_profile_button));
        popup.getMenuInflater().inflate(R.menu.user_profile_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_profile) {
                // Navigate to Profile Activity
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                return true;
            } else if (itemId == R.id.menu_routine) {
                startActivity(new Intent(HomeActivity.this, UserRoutineActivity.class));
                return true;
            } else if (itemId == R.id.menu_logout) {
                // Handle logout
                handleLogout();
                return true;
            }
            return false;
        });

        popup.show();
    }

    private void openMainMenu() {
        // Create and show a popup menu or drawer menu
        PopupMenu popup = new PopupMenu(this, findViewById(R.id.menu_button));
        popup.getMenuInflater().inflate(R.menu.main_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_profile) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                return true;
            } else if (itemId == R.id.menu_routine) {
                // Navigate to orders activity
                return true;
            } else if (itemId == R.id.menu_logout) {
                // Handle logout
                SignInActivity.clearStoredValues(this);
                startActivity(new Intent(HomeActivity.this, SignInActivity.class));
                finish();
                return true;
            }
            return false;
        });

        popup.show();
    }

    private void handleLogout() {
        // Clear stored user data
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        // Navigate back to sign in
        Intent intent = new Intent(HomeActivity.this, SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void setupSpinners() {
        // Setup skin type spinner
        ArrayAdapter<String> skinTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, skinTypes);
        skinTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        skinTypeSpinner.setAdapter(skinTypeAdapter);

        // Setup category spinner
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        // Spinner listeners
        skinTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedSkinType = skinTypes.get(position);
                String selectedCategory = categorySpinner.getSelectedItem().toString();
                loadFilteredProducts(selectedSkinType, selectedCategory);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = categories.get(position);
                String selectedSkinType = skinTypeSpinner.getSelectedItem().toString();
                loadFilteredProducts(selectedSkinType, selectedCategory);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupSearch() {
        searchButton.setOnClickListener(v -> {
            String query = searchEditText.getText().toString().trim();
            if (query.isEmpty()) {
                Toast.makeText(HomeActivity.this, "Please enter a search query", Toast.LENGTH_SHORT).show();
                loadProducts(); // Reset to all products
                return;
            }
            searchProduct(query);
        });
    }

    // Sửa lại giống ManagerProductsActivity: dùng updateData thay vì tạo adapter mới
    private void searchProduct(String query) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ProductResponse> call = query.matches("\\d+") ?
                apiService.getFilterProductById(query) :
                apiService.getProductsByName(query);

        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Product> products = response.body().getData();
                    adapter.updateData(products);
                } else {
                    Toast.makeText(HomeActivity.this, "No products found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Sửa lại giống ManagerProductsActivity: dùng updateData thay vì tạo adapter mới
    private void loadFilteredProducts(String skinType, String category) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ProductResponse> call;

        if (skinType.equals("All") && category.equals("All")) {
            call = apiService.getProducts();
        } else if (!skinType.equals("All")) {
            call = apiService.getProductsBySkinType(skinType);
        } else {
            call = apiService.getProductsByCategory(category);
        }

        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    adapter.updateData(response.body().getData());
                } else {
                    Toast.makeText(HomeActivity.this, "No products available", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProducts() {
        loadFilteredProducts("All", "All");
    }
}