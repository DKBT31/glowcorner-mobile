package com.example.mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.mobile.Adapter.OrderAdapter;
import com.example.mobile.Api.ApiClient;
import com.example.mobile.Api.ApiService;
import com.example.mobile.Models.Order;
import com.example.mobile.Models.OrderDetail;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    private static final int EDIT_PROFILE_REQUEST_CODE = 1001;
    private ImageView profileAvatarImageView;
    private TextView profileFullnameTextView;
    private TextView profileEmailTextView;
    private TextView profilePhoneTextView;
    private TextView profileAddressTextView;
    private TextView profileSkinTypeTextView;
    private TextView profileSkinCareRoutineTextView;
    private Button editProfileButton;
    private ListView orderListView;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize UI components
        profileAvatarImageView = findViewById(R.id.profile_avatar);
        profileFullnameTextView = findViewById(R.id.profile_fullname);
        profileEmailTextView = findViewById(R.id.profile_email);
        profilePhoneTextView = findViewById(R.id.profile_phone);
        profileAddressTextView = findViewById(R.id.profile_address);
        profileSkinTypeTextView = findViewById(R.id.profile_skin_type);
        profileSkinCareRoutineTextView = findViewById(R.id.profile_skin_care_routine);
        editProfileButton = findViewById(R.id.btn_edit_profile);
        orderListView = findViewById(R.id.order_list_view);

        // Initialize order list and adapter
        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(this, orderList);
        orderListView.setAdapter(orderAdapter);

        // Set up button listener
        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivityForResult(intent, EDIT_PROFILE_REQUEST_CODE);
        });

        // Load user profile and orders
        loadUserProfile();
        loadOrders();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_PROFILE_REQUEST_CODE && resultCode == RESULT_OK) {
            // Reload profile data when returning from edit
            loadUserProfile();
        }
    }

    private void loadUserProfile() {
        String userId = SignInActivity.getStoredValue(this, "userID");
        if (userId == null) {
            Toast.makeText(this, "User not logged in. Please log in.", Toast.LENGTH_SHORT).show();
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
                        Log.d(TAG, "User Profile Response: " + responseString);

                        JSONObject jsonObject = new JSONObject(responseString);
                        if (jsonObject.getBoolean("success") && jsonObject.getInt("status") == 200) {
                            JsonParser parser = new JsonParser();
                            JsonObject rootObj = parser.parse(responseString).getAsJsonObject();
                            JsonObject dataObject = rootObj.getAsJsonObject("data");

                            // Parse fields
                            String fullname = dataObject.has("fullName") && !dataObject.get("fullName").isJsonNull()
                                    ? dataObject.get("fullName").getAsString()
                                    : "";
                            String email = dataObject.has("email") && !dataObject.get("email").isJsonNull()
                                    ? dataObject.get("email").getAsString()
                                    : "Email not available";
                            String phone = dataObject.has("phone") && !dataObject.get("phone").isJsonNull()
                                    ? dataObject.get("phone").getAsString()
                                    : "Phone not available";
                            String address = dataObject.has("address") && !dataObject.get("address").isJsonNull()
                                    ? dataObject.get("address").getAsString()
                                    : "Address not available";
                            String skinType = dataObject.has("skinType") && !dataObject.get("skinType").isJsonNull()
                                    ? dataObject.get("skinType").getAsString()
                                    : "Skin Type not available";

                            // Handle skinCareRoutine as a JsonObject
                            String skinCareRoutine;
                            if (dataObject.has("skinCareRoutine") && !dataObject.get("skinCareRoutine").isJsonNull()) {
                                JsonObject routineObj = dataObject.get("skinCareRoutine").getAsJsonObject();
                                skinCareRoutine = routineObj.has("routineName")
                                        && !routineObj.get("routineName").isJsonNull()
                                                ? routineObj.get("routineName").getAsString()
                                                : "Skin Care Routine: Not set";
                            } else {
                                skinCareRoutine = "Skin Care Routine: Not set";
                            }

                            String avatarUrl = dataObject.has("avatar_url")
                                    && !dataObject.get("avatar_url").isJsonNull()
                                            ? dataObject.get("avatar_url").getAsString()
                                            : null;

                            // Set text fields
                            profileFullnameTextView.setText(fullname.isEmpty() ? "Name not available" : fullname);
                            profileEmailTextView.setText(email);
                            profilePhoneTextView.setText(phone);
                            profileAddressTextView.setText(address);
                            profileSkinTypeTextView.setText(skinType);
                            profileSkinCareRoutineTextView.setText(skinCareRoutine);

                            // Load avatar image with Glide
                            if (avatarUrl != null) {
                                Glide.with(ProfileActivity.this)
                                        .load(avatarUrl)
                                        .placeholder(R.drawable.ava)
                                        .error(R.drawable.ava)
                                        .into(profileAvatarImageView);
                            } else {
                                profileAvatarImageView.setImageResource(R.drawable.ava);
                            }

                            // Save address to SharedPreferences if not already saved
                            String storedAddress = SignInActivity.getStoredValue(ProfileActivity.this, "userAddress");
                            if (address != null && !address.equals("Address not available")
                                    && (storedAddress == null || storedAddress.isEmpty())) {
                                SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("userAddress", address);
                                editor.apply();
                                Log.d(TAG, "Saved new userAddress to SharedPreferences: " + address);
                            }
                        } else {
                            Toast.makeText(ProfileActivity.this,
                                    "Failed to load profile: " + jsonObject.getString("description"),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "Error parsing profile response: " + e.getMessage());
                        Toast.makeText(ProfileActivity.this, "Error reading profile data", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to load profile. Status: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "API Call Failed for profile: " + t.getMessage(), t);
                Toast.makeText(ProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadOrders() {
        String userId = SignInActivity.getStoredValue(this, "userID");
        String jwtToken = SignInActivity.getStoredValue(this, "jwtToken");

        if (userId == null || jwtToken == null) {
            Toast.makeText(this, "Please log in to view orders.", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ResponseBody> call = apiService.getCustomerOrders("Bearer " + jwtToken, userId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseString = response.body().string();
                        Log.d(TAG, "Order Response: " + responseString);

                        JSONObject jsonObject = new JSONObject(responseString);
                        if (jsonObject.getBoolean("success") && jsonObject.getInt("status") == 200) {
                            JsonParser parser = new JsonParser();
                            JsonObject rootObj = parser.parse(responseString).getAsJsonObject();
                            JsonArray dataArray = rootObj.getAsJsonArray("data");

                            orderList.clear();
                            for (JsonElement element : dataArray) {
                                JsonObject orderObj = element.getAsJsonObject();
                                String orderId = orderObj.get("orderID").getAsString();
                                String customerId = orderObj.get("customerID").getAsString();
                                String customerName = orderObj.get("customerName").getAsString();
                                String orderDate = orderObj.get("orderDate").getAsString();
                                String status = orderObj.get("status").getAsString();
                                double totalAmount = orderObj.get("totalAmount").getAsDouble();
                                double discountedTotalAmount = orderObj.get("discountedTotalAmount").getAsDouble();

                                JsonArray detailsArray = orderObj.getAsJsonArray("orderDetails");
                                List<OrderDetail> orderDetails = new ArrayList<>();
                                for (JsonElement detailElement : detailsArray) {
                                    JsonObject detailObj = detailElement.getAsJsonObject();
                                    String detailOrderId = detailObj.get("orderID").getAsString();
                                    String productId = detailObj.get("productID").getAsString();
                                    String productName = detailObj.get("productName").getAsString();
                                    int quantity = detailObj.get("quantity").getAsInt();
                                    double productPrice = detailObj.get("productPrice").getAsDouble();

                                    // Get discounted amounts if available
                                    Double detailDiscountedTotal = null;
                                    if (detailObj.has("discountedTotalAmount")
                                            && !detailObj.get("discountedTotalAmount").isJsonNull()) {
                                        detailDiscountedTotal = detailObj.get("discountedTotalAmount").getAsDouble();
                                    }

                                    Double discountPercentage = null;
                                    if (detailObj.has("discountPercentage")
                                            && !detailObj.get("discountPercentage").isJsonNull()) {
                                        discountPercentage = detailObj.get("discountPercentage").getAsDouble();
                                    }

                                    String discountName = null;
                                    if (detailObj.has("discountName") && !detailObj.get("discountName").isJsonNull()) {
                                        discountName = detailObj.get("discountName").getAsString();
                                    }

                                    double detailTotalAmount = totalAmount; // Default fallback
                                    if (detailObj.has("totalAmount")) {
                                        detailTotalAmount = detailObj.get("totalAmount").getAsDouble();
                                    }

                                    orderDetails.add(new OrderDetail(detailOrderId, productId, productName, quantity,
                                            productPrice, discountName, discountPercentage, detailTotalAmount,
                                            detailDiscountedTotal));
                                }

                                orderList.add(
                                        new Order(orderId, customerId, customerName, orderDate, status, totalAmount,
                                                discountedTotalAmount, orderDetails, null, null, null, null, null));
                            }

                            orderAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(ProfileActivity.this,
                                    "Failed to load orders: " + jsonObject.getString("description"), Toast.LENGTH_SHORT)
                                    .show();
                        }
                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "Error parsing order response: " + e.getMessage());
                        Toast.makeText(ProfileActivity.this, "Error reading order data", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to load orders. Status: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "API Call Failed for orders: " + t.getMessage(), t);
                Toast.makeText(ProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
