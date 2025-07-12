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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile.Adapter.OrderDetailAdapter;
import com.example.mobile.Api.ApiClient;
import com.example.mobile.Api.ApiService;
import com.example.mobile.Models.OrderDetail;
import com.example.mobile.Models.OrderResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderDetailActivity extends AppCompatActivity {
    private static final String TAG = "OrderDetailActivity";
    private TextView orderIdTextView, orderDateTextView, customerNameTextView, totalAmountTextView, statusTextView,
            unpaidMessageTextView;
    private RecyclerView orderDetailsRecyclerView;
    private Button checkoutButton;
    private OrderDetailAdapter orderDetailAdapter;
    private List<OrderDetail> orderDetailsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        // Initialize UI components
        orderIdTextView = findViewById(R.id.order_id_detail);
        orderDateTextView = findViewById(R.id.order_date_detail);
        customerNameTextView = findViewById(R.id.order_customer_name_detail);
        totalAmountTextView = findViewById(R.id.order_total_amount_detail);
        statusTextView = findViewById(R.id.order_status_detail);
        unpaidMessageTextView = findViewById(R.id.unpaid_message);
        checkoutButton = findViewById(R.id.checkout_button);
        orderDetailsRecyclerView = findViewById(R.id.order_details_recycler_view);

        // Initialize RecyclerView
        orderDetailsList = new ArrayList<>();
        orderDetailAdapter = new OrderDetailAdapter(this, orderDetailsList);
        orderDetailsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderDetailsRecyclerView.setAdapter(orderDetailAdapter);

        // Get orderId from Intent
        Intent intent = getIntent();
        String orderId = intent.getStringExtra("orderId");
        if (orderId != null) {
            loadOrderDetails(orderId);
        } else {
            Toast.makeText(this, "Order ID not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadOrderDetails(String orderId) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ResponseBody> call = apiService.getOrderDetails(orderId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseString = response.body().string();
                        Log.d(TAG, "Order Details Response: " + responseString);

                        JSONObject jsonObject = new JSONObject(responseString);
                        if (jsonObject.getBoolean("success") && jsonObject.getInt("status") == 200) {
                            JsonParser parser = new JsonParser();
                            JsonObject rootObj = parser.parse(responseString).getAsJsonObject();
                            JsonObject dataObject = rootObj.getAsJsonObject("data");

                            // Set order information
                            String status = dataObject.get("status").getAsString();
                            orderIdTextView.setText(dataObject.get("orderID").getAsString());
                            orderDateTextView.setText(dataObject.get("orderDate").getAsString());
                            String customerName = dataObject.get("customerName").isJsonNull()
                                    ? "Unknown Customer"
                                    : dataObject.get("customerName").getAsString();
                            customerNameTextView.setText(customerName);
                            double totalAmount = dataObject.get("totalAmount").getAsDouble();

                            // Use discounted amount if available, otherwise use total amount
                            double tempDisplayAmount = totalAmount;
                            if (dataObject.has("discountedTotalAmount")
                                    && !dataObject.get("discountedTotalAmount").isJsonNull()) {
                                double discountedAmount = dataObject.get("discountedTotalAmount").getAsDouble();
                                if (discountedAmount > 0) {
                                    tempDisplayAmount = discountedAmount;
                                }
                            }
                            final double displayAmount = tempDisplayAmount; // Make it final for lambda

                            totalAmountTextView.setText("$" + String.format("%.2f", displayAmount));
                            statusTextView.setText(status);

                            // Set status background color
                            setStatusBackgroundColor(status);

                            // Check user role to hide checkout for staff
                            String userRole = SignInActivity.getStoredValue(OrderDetailActivity.this, "userRole");
                            if ("STAFF".equals(userRole)) {
                                unpaidMessageTextView.setVisibility(View.GONE);
                                checkoutButton.setVisibility(View.GONE);
                            } else if ("PENDING".equals(status)) {
                                unpaidMessageTextView.setVisibility(View.VISIBLE);
                                checkoutButton.setVisibility(View.VISIBLE);
                                checkoutButton.setOnClickListener(v -> handleCheckout(orderId, displayAmount));
                            } else {
                                unpaidMessageTextView.setVisibility(View.GONE);
                                checkoutButton.setVisibility(View.GONE);
                            }

                            // Set order details
                            orderDetailsList.clear();
                            JsonArray detailsArray = dataObject.getAsJsonArray("orderDetails");
                            for (JsonElement element : detailsArray) {
                                JsonObject detailObj = element.getAsJsonObject();
                                orderDetailsList.add(new OrderDetail(
                                        detailObj.get("orderID").getAsString(),
                                        detailObj.get("productID").getAsString(),
                                        detailObj.get("productName").getAsString(),
                                        detailObj.get("quantity").getAsInt(),
                                        detailObj.get("productPrice").getAsDouble(),
                                        detailObj.get("discountName").isJsonNull() ? null
                                                : detailObj.get("discountName").getAsString(),
                                        detailObj.get("discountPercentage").isJsonNull() ? null
                                                : detailObj.get("discountPercentage").getAsDouble(),
                                        detailObj.get("totalAmount").getAsDouble(),
                                        detailObj.get("discountedTotalAmount").isJsonNull() ? null
                                                : detailObj.get("discountedTotalAmount").getAsDouble()));
                            }
                            orderDetailAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(OrderDetailActivity.this,
                                    "Failed to load order details: " + jsonObject.getString("description"),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "Error parsing order details response: " + e.getMessage());
                        Toast.makeText(OrderDetailActivity.this, "Error reading order data", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(OrderDetailActivity.this, "Failed to load order details. Status: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "API Call Failed for order details: " + t.getMessage(), t);
                Toast.makeText(OrderDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setStatusBackgroundColor(String status) {
        int colorResId;
        switch (status.toUpperCase()) {
            case "PENDING":
                colorResId = R.color.pending_color;
                break;
            case "COMPLETED":
            case "DELIVERED":
                colorResId = R.color.completed_color;
                break;
            case "PROCESSING":
                colorResId = R.color.processing_color;
                break;
            case "CANCELLED":
                colorResId = R.color.cancelled_color;
                break;
            default:
                colorResId = R.color.default_color;
                break;
        }
        statusTextView.setBackgroundColor(ContextCompat.getColor(this, colorResId));
    }

    private void handleCheckout(String orderId, double totalAmount) {
        String userID = SignInActivity.getStoredValue(this, "userID");
        String jwtToken = SignInActivity.getStoredValue(this, "jwtToken");
        if (userID == null || jwtToken == null) {
            Toast.makeText(this, "Please log in to proceed with checkout", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if user has an address
        String address = SignInActivity.getStoredValue(this, "userAddress");
        if (address == null || address.isEmpty()) {
            Toast.makeText(this, "Please add your address in Profile", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(OrderDetailActivity.this, ProfileActivity.class));
            return;
        }

        // Since order already exists, we may need to retrieve or update payment intent
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        JsonObject orderData = new JsonObject();
        orderData.addProperty("orderID", orderId);
        orderData.addProperty("totalAmount", totalAmount);
        orderData.addProperty("status", "PENDING");

        Call<OrderResponse> call = apiService.createOrder("Bearer " + jwtToken, userID, orderData);
        call.enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    String clientSecret = response.body().getData().getPaymentIntentId();
                    String returnedOrderID = response.body().getData().getOrderID();
                    double discountedTotalAmount = response.body().getData().getDiscountedTotalAmount();

                    // Store orderID in SharedPreferences
                    SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("orderID", returnedOrderID);
                    editor.apply();

                    Log.d(TAG, "Received clientSecret = " + clientSecret);
                    Intent intent = new Intent(OrderDetailActivity.this, CheckoutActivity.class);
                    intent.putExtra("clientSecret", clientSecret);
                    intent.putExtra("totalAmount", totalAmount);
                    intent.putExtra("discountedAmount", discountedTotalAmount);
                    startActivity(intent);
                } else {
                    Toast.makeText(OrderDetailActivity.this, "Failed to prepare checkout", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                Toast.makeText(OrderDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}