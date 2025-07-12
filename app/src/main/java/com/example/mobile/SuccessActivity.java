package com.example.mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile.Adapter.OrderDetailsSuccessAdapter;
import com.example.mobile.Api.ApiClient;
import com.example.mobile.Api.ApiService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SuccessActivity extends AppCompatActivity {
    private static final String TAG = "SuccessActivity";
    private TextView orderIdTextView, customerNameTextView, orderDateTextView, totalAmountTextView;
    private RecyclerView orderDetailsRecyclerView;
    private OrderDetailsSuccessAdapter adapter;
    private Button backToHomeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        orderIdTextView = findViewById(R.id.order_id);
        customerNameTextView = findViewById(R.id.customer_name);
        orderDateTextView = findViewById(R.id.order_date);
        totalAmountTextView = findViewById(R.id.total_amount);
        orderDetailsRecyclerView = findViewById(R.id.order_details_recycler_view);
        backToHomeButton = findViewById(R.id.back_to_home_button);

        orderDetailsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderDetailsSuccessAdapter();
        orderDetailsRecyclerView.setAdapter(adapter);

        backToHomeButton.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            String orderID = prefs.getString("orderID", null);
            if (orderID == null) {
                Toast.makeText(SuccessActivity.this, "Order ID not found.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get JWT token for authorization
            String jwtToken = SignInActivity.getStoredValue(this, "jwtToken");
            String authHeader = jwtToken != null && !jwtToken.isEmpty()
                    ? (jwtToken.startsWith("Bearer ") ? jwtToken : "Bearer " + jwtToken)
                    : "";

            ApiService apiService = ApiClient.getClient().create(ApiService.class);
            String statusJson = "{\"status\":\"COMPLETED\"}";
            RequestBody statusBody = RequestBody.create(statusJson,
                    okhttp3.MediaType.get("application/json; charset=utf-8"));
            Call<ResponseBody> call = apiService.updateOrderStatus(authHeader, orderID, statusBody);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        if (response.isSuccessful()) {
                            Toast.makeText(SuccessActivity.this, "Order status updated to COMPLETED!",
                                    Toast.LENGTH_SHORT).show();
                            // Clear orderID from SharedPreferences
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.remove("orderID");
                            editor.apply();
                            // Navigate to HomeActivity
                            Intent intent = new Intent(SuccessActivity.this, HomeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(SuccessActivity.this, "Failed to update order status.", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(SuccessActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(SuccessActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        loadOrderDetails();
    }

    private void loadOrderDetails() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String orderID = prefs.getString("orderID", null);

        if (orderID == null) {
            // Show basic success info even without orderID
            orderIdTextView.setText("Order ID: Processing...");
            customerNameTextView.setText("Customer: " + SignInActivity.getStoredValue(this, "username"));
            orderDateTextView
                    .setText("Order Date: " + java.text.DateFormat.getDateInstance().format(new java.util.Date()));
            totalAmountTextView.setText("Total Amount: Payment Successful");
            Toast.makeText(this, "Order details are being processed", Toast.LENGTH_SHORT).show();
            return;
        }

        // Try to get order details from API
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ResponseBody> call = apiService.getOrderDetails(orderID);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseString = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseString);
                        JSONObject data = jsonObject.getJSONObject("data");

                        // Get the paid amount from SharedPreferences (what user actually paid)
                        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                        float paidAmount = prefs.getFloat("paidAmount", 0.0f);
                        float originalAmount = prefs.getFloat("originalAmount", 0.0f);
                        float discountedAmount = prefs.getFloat("discountedAmount", 0.0f);

                        Log.d(TAG, "💰 Payment amounts - Paid: $" + paidAmount +
                                ", Original: $" + originalAmount + ", Discounted: $" + discountedAmount);

                        String orderID = data.getString("orderID");
                        String customerName = data.getString("customerName");
                        String orderDate = data.getString("orderDate");
                        double backendTotalAmount = data.getDouble("totalAmount");

                        // Use the actual paid amount instead of backend totalAmount
                        double displayAmount = paidAmount > 0 ? paidAmount : backendTotalAmount;

                        orderIdTextView.setText("Order ID: " + orderID);
                        customerNameTextView.setText("Customer: " + customerName);
                        orderDateTextView.setText("Order Date: " + orderDate);
                        totalAmountTextView.setText("Total Paid: $" + String.format("%.2f", displayAmount));

                        JSONArray orderDetailsArray = data.getJSONArray("orderDetails");
                        List<OrderDetailsSuccessAdapter.OrderDetail> orderDetails = new ArrayList<>();
                        for (int i = 0; i < orderDetailsArray.length(); i++) {
                            JSONObject detail = orderDetailsArray.getJSONObject(i);
                            OrderDetailsSuccessAdapter.OrderDetail orderDetail = new OrderDetailsSuccessAdapter.OrderDetail(
                                    detail.getString("productName"),
                                    detail.getInt("quantity"),
                                    detail.getDouble("productPrice"),
                                    detail.getDouble("totalAmount"));
                            orderDetails.add(orderDetail);
                        }

                        adapter.setOrderDetails(orderDetails);
                        adapter.notifyDataSetChanged();
                    } else {
                        // Show fallback info if API fails
                        showFallbackOrderInfo(orderID);
                    }
                } catch (Exception e) {
                    // Show fallback info on error
                    showFallbackOrderInfo(orderID);
                    Toast.makeText(SuccessActivity.this, "Using cached order info", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // Show fallback info on network failure
                showFallbackOrderInfo(orderID);
                Toast.makeText(SuccessActivity.this, "Order completed successfully (offline mode)", Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    private void showFallbackOrderInfo(String orderID) {
        runOnUiThread(() -> {
            // Get the paid amount from SharedPreferences
            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            float paidAmount = prefs.getFloat("paidAmount", 0.0f);

            orderIdTextView.setText("Order ID: " + (orderID != null ? orderID : "Processing..."));
            customerNameTextView.setText("Customer: " + SignInActivity.getStoredValue(this, "username"));
            orderDateTextView
                    .setText("Order Date: " + java.text.DateFormat.getDateInstance().format(new java.util.Date()));

            // Show the actual paid amount or fallback message
            if (paidAmount > 0) {
                totalAmountTextView.setText("Total Paid: $" + String.format("%.2f", paidAmount));
            } else {
                totalAmountTextView.setText("Total Amount: Payment Successful");
            }

            // Show a simple success message in the RecyclerView
            List<OrderDetailsSuccessAdapter.OrderDetail> fallbackDetails = new ArrayList<>();
            fallbackDetails.add(new OrderDetailsSuccessAdapter.OrderDetail(
                    "Order Items", 1, 0.0, 0.0));
            adapter.setOrderDetails(fallbackDetails);
            adapter.notifyDataSetChanged();
        });
    }
}