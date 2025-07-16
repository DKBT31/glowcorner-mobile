package com.example.mobile;

import android.content.Intent;
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

import com.example.mobile.Adapter.CartAdapter;
import com.example.mobile.Api.ApiClient;
import com.example.mobile.Api.ApiService;
import com.example.mobile.Models.CartResponse;
import com.example.mobile.Models.OrderResponse;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity {
    private static final String TAG = "CartActivity";
    private RecyclerView recyclerView;
    private CartAdapter adapter;
    private TextView totalAmountTextView;
    private TextView discountedTotalAmountTextView;
    private TextView finalTotalAmountTextView;
    private TextView addressWarningTextView;
    private Button enterInformationButton;
    private Button checkoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        recyclerView = findViewById(R.id.cart_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize all TextViews properly
        totalAmountTextView = findViewById(R.id.total_amount);
        discountedTotalAmountTextView = findViewById(R.id.discounted_total_amount);
        finalTotalAmountTextView = findViewById(R.id.final_total_amount);
        addressWarningTextView = findViewById(R.id.address_warning);
        enterInformationButton = findViewById(R.id.btn_enter_information);
        checkoutButton = findViewById(R.id.btn_checkout);

        // Set cart title with user's full name
        setCartTitle();

        // Initialize adapter with proper listener
        adapter = new CartAdapter();
        adapter.setOnItemActionListener(new CartAdapter.OnItemActionListener() {
            @Override
            public void onQuantityChanged(String productId, int newQuantity) {
                if (newQuantity > 0) {
                    updateCartItemQuantity(productId, newQuantity);
                } else {
                    removeItemFromCart(productId);
                }
            }
        });
        recyclerView.setAdapter(adapter);

        NavigationManager.setupNavigation(this, findViewById(R.id.bottom_navigation));

        loadUserAddressFromApi();

        enterInformationButton.setOnClickListener(v -> {
            startActivity(new Intent(CartActivity.this, ProfileActivity.class));
        });

        checkoutButton.setOnClickListener(v -> handleCheckout());

        loadCartItems();
    }

    private void setCartTitle() {
        String userId = SignInActivity.getStoredValue(this, "userID");
        if (userId == null)
            return;

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ResponseBody> call = apiService.getUserProfile(userId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseString = response.body().string();
                        org.json.JSONObject jsonObject = new org.json.JSONObject(responseString);
                        org.json.JSONObject userData = jsonObject.getJSONObject("data");

                        String fullName = userData.optString("fullName", "");
                        TextView cartTitle = findViewById(R.id.cart_title);
                        if (cartTitle != null) {
                            if (!fullName.isEmpty()) {
                                cartTitle.setText(fullName + "'s Cart");
                            } else {
                                cartTitle.setText("Your Cart");
                            }
                        }
                    } else {
                        TextView cartTitle = findViewById(R.id.cart_title);
                        if (cartTitle != null) {
                            cartTitle.setText("Your Cart");
                        }
                    }
                } catch (Exception e) {
                    TextView cartTitle = findViewById(R.id.cart_title);
                    if (cartTitle != null) {
                        cartTitle.setText("Your Cart");
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                TextView cartTitle = findViewById(R.id.cart_title);
                if (cartTitle != null) {
                    cartTitle.setText("Your Cart");
                }
            }
        });
    }

    private void loadUserAddressFromApi() {
        String userId = SignInActivity.getStoredValue(this, "userID");
        if (userId == null)
            return;

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ResponseBody> call = apiService.getUserProfile(userId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseString = response.body().string();
                        org.json.JSONObject jsonObject = new org.json.JSONObject(responseString);
                        String address = jsonObject.getJSONObject("data").optString("address", "");

                        // Update UI
                        addressWarningTextView
                                .setText(address.isEmpty() ? "Please add your address." : "Address: " + address);

                        // Update SharedPreferences so handleCheckout() can find the address
                        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("userAddress", address);
                        editor.apply();

                        Log.d("CartActivity", "Address loaded and saved to SharedPreferences: " + address);
                    } else {
                        addressWarningTextView.setText("Failed to load address");
                    }
                } catch (Exception e) {
                    addressWarningTextView.setText("Error loading address");
                    Log.e("CartActivity", "Error parsing address response", e);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                addressWarningTextView.setText("Network error: " + t.getMessage());
                Log.e("CartActivity", "Network error loading address", t);
            }
        });
    }

    private void updateCartItemQuantity(String productId, int newQuantity) {
        String userID = SignInActivity.getStoredValue(this, "userID");
        if (userID == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            return;
        }
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ResponseBody> call = apiService.updateCartItem(userID, productId, newQuantity);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    loadCartItems();
                } else {
                    Toast.makeText(CartActivity.this, "Failed to update quantity", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(CartActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeItemFromCart(String productId) {
        String userID = SignInActivity.getStoredValue(this, "userID");
        if (userID == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            return;
        }
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ResponseBody> call = apiService.removeFromCart(userID, productId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    loadCartItems();
                } else {
                    Toast.makeText(CartActivity.this, "Failed to remove item", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(CartActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleCheckout() {
        String userID = SignInActivity.getStoredValue(this, "userID");
        String jwtToken = SignInActivity.getStoredValue(this, "jwtToken");
        if (userID == null || jwtToken == null)
            return;

        // Check if cart has items
        if (adapter.getItemCount() == 0) {
            Toast.makeText(this, "Your cart is empty! Add some products first.", Toast.LENGTH_LONG).show();
            return;
        }

        // Check if user has an address
        String address = SignInActivity.getStoredValue(this, "userAddress");
        if (address == null || address.isEmpty()) {
            Toast.makeText(this, "Please add your address in Profile", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(CartActivity.this, ProfileActivity.class));
            return;
        }

        // Declare variables as final outside try block
        final double[] amounts = new double[2]; // [totalAmount, discountedTotalAmount]

        try {
            String totalText = totalAmountTextView.getText().toString();
            TextView finalTotalTextView = findViewById(R.id.final_total_amount);
            String finalTotalText = finalTotalTextView.getText().toString();

            // Extract original total (subtotal before discount)
            amounts[0] = Double.parseDouble(totalText.replace("$", ""));

            // Extract final total (after discount)
            amounts[1] = Double.parseDouble(finalTotalText.replace("$", ""));

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Error calculating total", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObject orderData = new JsonObject();
        orderData.addProperty("customerID", userID);
        orderData.addProperty("orderDate", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        orderData.addProperty("status", "PENDING");
        orderData.addProperty("totalAmount", amounts[0]);
        orderData.addProperty("discountedTotalAmount", amounts[1]);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<OrderResponse> call = apiService.createOrder("Bearer " + jwtToken, userID, orderData);
        call.enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    String orderID = response.body().getData().getOrderID();

                    // Store orderID for later use
                    SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("orderID", orderID);
                    editor.apply();

                    // Now create Stripe payment intent
                    createPaymentIntent(amounts[0], amounts[1]);
                } else {
                    Toast.makeText(CartActivity.this, "Order creation failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                Toast.makeText(CartActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCartItems() {
        String userID = SignInActivity.getStoredValue(this, "userID");
        if (userID == null)
            return;

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<CartResponse> call = apiService.getCart(userID);

        call.enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(Call<CartResponse> call, Response<CartResponse> response) {
                if (response.body() != null) {
                    List<CartResponse.Item> items = response.body().getItems();
                    adapter.setItems(items);

                    // Calculate totals properly
                    double originalTotal = 0;
                    double discountedTotal = 0;

                    for (CartResponse.Item item : items) {
                        originalTotal += item.getTotalAmount();

                        // If item has discount, use discounted amount, otherwise use original
                        if (item.getDiscountedTotalAmount() != null) {
                            discountedTotal += item.getDiscountedTotalAmount();
                        } else {
                            discountedTotal += item.getTotalAmount();
                        }
                    }

                    // Calculate discount amount (what was saved)
                    double discountAmount = originalTotal - discountedTotal;

                    // Update the UI with proper labels
                    totalAmountTextView.setText("$" + String.format("%.2f", originalTotal));

                    if (discountAmount > 0) {
                        discountedTotalAmountTextView.setText("-$" + String.format("%.2f", discountAmount));
                    } else {
                        discountedTotalAmountTextView.setText("$0.00");
                    }

                    // Update the final total TextView (you need to add this to your layout)
                    TextView finalTotalTextView = findViewById(R.id.final_total_amount);
                    if (finalTotalTextView != null) {
                        finalTotalTextView.setText("$" + String.format("%.2f", discountedTotal));
                    }
                }
            }

            @Override
            public void onFailure(Call<CartResponse> call, Throwable t) {
                Toast.makeText(CartActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createPaymentIntent(double totalAmount, double discountedAmount) {
        Log.d(TAG, "Creating payment intent - Total: " + totalAmount + ", Discounted: " + discountedAmount);

        // Use discounted amount if available, otherwise use total amount
        double paymentAmount = discountedAmount > 0 ? discountedAmount : totalAmount;

        // Convert to cents (Stripe requires amount in smallest currency unit)
        long amountInCents = Math.round(paymentAmount * 100);

        Log.d(TAG, "Payment amount: $" + paymentAmount + " (" + amountInCents + " cents)");

        JsonObject paymentRequest = new JsonObject();
        paymentRequest.addProperty("amount", amountInCents);
        paymentRequest.addProperty("currency", "usd");

        Log.d(TAG, "Payment request: " + paymentRequest.toString());

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ResponseBody> call = apiService.createPaymentIntent(paymentRequest);

        Log.d(TAG, "Making payment intent API call...");

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "Payment intent response received - Code: " + response.code());

                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        Log.d(TAG, "Payment intent response body: " + responseBody);

                        JsonObject jsonResponse = new com.google.gson.JsonParser().parse(responseBody)
                                .getAsJsonObject();

                        if (jsonResponse.get("success").getAsBoolean()) {
                            JsonObject data = jsonResponse.getAsJsonObject("data");
                            String clientSecret = data.get("clientSecret").getAsString();

                            Log.d(TAG, "Payment intent created successfully - ClientSecret: " +
                                    (clientSecret != null
                                            ? clientSecret.substring(0, Math.min(20, clientSecret.length())) + "..."
                                            : "NULL"));

                            // Now start checkout activity with real client secret
                            Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
                            intent.putExtra("clientSecret", clientSecret);
                            intent.putExtra("totalAmount", totalAmount);
                            intent.putExtra("discountedAmount", discountedAmount);

                            Log.d(TAG, "Starting CheckoutActivity with intent extras");
                            startActivity(intent);
                        } else {
                            String errorMsg = jsonResponse.has("message") ? jsonResponse.get("message").getAsString()
                                    : "Unknown error";
                            Log.e(TAG, "Payment intent creation failed: " + errorMsg);
                            Toast.makeText(CartActivity.this, "Payment setup failed: " + errorMsg, Toast.LENGTH_LONG)
                                    .show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing payment intent response: " + e.getMessage(), e);
                        Toast.makeText(CartActivity.this, "Payment setup error: " + e.getMessage(), Toast.LENGTH_LONG)
                                .show();
                    }
                } else {
                    Log.e(TAG, "Payment intent API call failed - Code: " + response.code());
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string()
                                : "No error body";
                        Log.e(TAG, "Error response body: " + errorBody);
                        Toast.makeText(CartActivity.this,
                                "Payment setup failed (Code: " + response.code() + "): " + errorBody, Toast.LENGTH_LONG)
                                .show();
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body: " + e.getMessage());
                        Toast.makeText(CartActivity.this, "Payment setup failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Payment intent creation failed: " + t.getMessage(), t);
                Toast.makeText(CartActivity.this, "Payment setup error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}